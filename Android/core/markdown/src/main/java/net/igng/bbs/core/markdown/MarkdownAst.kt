package net.igng.bbs.core.markdown

data class TocEntry(
    val level: Int,
    val title: String,
    val blockIndex: Int,
)

data class TaskListItem(
    val checked: Boolean,
    val inlines: List<InlineSpan>,
)

sealed interface InlineSpan {
    data class Text(val text: String) : InlineSpan
    data class Bold(val children: List<InlineSpan>) : InlineSpan
    data class Italic(val children: List<InlineSpan>) : InlineSpan
    data class Strike(val children: List<InlineSpan>) : InlineSpan
    data class InlineCode(val code: String) : InlineSpan
    data class Highlight(val text: String) : InlineSpan
    data class Link(val label: String, val url: String) : InlineSpan
    data class Mention(val handle: String) : InlineSpan
}

sealed interface MarkdownBlock {
    data class Heading(
        val level: Int,
        val inlines: List<InlineSpan>,
        val plainText: String,
    ) : MarkdownBlock

    data class Paragraph(val inlines: List<InlineSpan>) : MarkdownBlock
    data class BulletList(val items: List<List<InlineSpan>>) : MarkdownBlock
    data class OrderedList(val start: Int, val items: List<List<InlineSpan>>) : MarkdownBlock
    data class TaskList(val items: List<TaskListItem>) : MarkdownBlock
    data class Blockquote(val lines: List<List<InlineSpan>>) : MarkdownBlock
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock
    data class Table(val headers: List<List<InlineSpan>>, val rows: List<List<List<InlineSpan>>>) : MarkdownBlock
    data object Divider : MarkdownBlock
    data class Image(val url: String, val alt: String, val rawUrl: String) : MarkdownBlock
    data class VideoEmbed(val url: String, val label: String) : MarkdownBlock
}

data class MarkdownDocument(
    val blocks: List<MarkdownBlock>,
    val toc: List<TocEntry>,
    val imageUrls: List<String>,
)

object MarkdownParser {

    fun parse(
        source: String,
        format: String = "markdown",
        resolveUrl: (String) -> String = { it },
    ): MarkdownDocument {
        val normalizedSource = if (format.equals("html", ignoreCase = true) || looksLikeHtml(source)) {
            htmlToMarkdown(source)
        } else {
            source
        }
        val lines = normalizedSource.replace("\r\n", "\n").replace('\r', '\n').split('\n')
        val blocks = mutableListOf<MarkdownBlock>()
        val toc = mutableListOf<TocEntry>()
        val images = mutableListOf<String>()

        var i = 0
        while (i < lines.size) {
            val rawLine = lines[i]
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) {
                i++
                continue
            }

            // Fenced code block ```lang
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                val fence = trimmed.take(3)
                val lang = trimmed.removePrefix(fence).trim()
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].trim().startsWith(fence)) {
                    codeLines.add(lines[i])
                    i++
                }
                if (i < lines.size) i++
                blocks.add(MarkdownBlock.CodeBlock(language = lang, code = codeLines.joinToString("\n")))
                continue
            }

            // Divider --- *** ___
            if (DIVIDER_RE.matches(trimmed)) {
                blocks.add(MarkdownBlock.Divider)
                i++
                continue
            }

            // Heading # .. ####
            val headingMatch = HEADING_RE.matchEntire(trimmed)
            if (headingMatch != null) {
                val level = headingMatch.groupValues[1].length.coerceIn(1, 4)
                val body = headingMatch.groupValues[2].trim().trimEnd('#').trim()
                val inlines = parseInlines(body)
                val plain = extractPlainText(inlines)
                val blockIdx = blocks.size
                blocks.add(MarkdownBlock.Heading(level = level, inlines = inlines, plainText = plain))
                if (plain.isNotBlank()) {
                    toc.add(TocEntry(level = level, title = plain, blockIndex = blockIdx))
                }
                i++
                continue
            }

            // Standalone image ![alt](url)
            val imgMatch = STANDALONE_IMAGE_RE.matchEntire(trimmed)
            if (imgMatch != null) {
                val alt = imgMatch.groupValues[1].trim()
                val rawUrl = imgMatch.groupValues[2].trim().substringBefore(' ')
                val resolved = resolveUrl(rawUrl)
                images.add(resolved)
                blocks.add(MarkdownBlock.Image(url = resolved, alt = alt, rawUrl = rawUrl))
                i++
                continue
            }

            // Video syntax :::video url ::: or @[video](url)
            val vidMatch = VIDEO_RE.matchEntire(trimmed)
            if (vidMatch != null) {
                val label = vidMatch.groupValues[1].ifBlank { "嵌入视频" }
                val url = resolveUrl(vidMatch.groupValues[2].trim())
                blocks.add(MarkdownBlock.VideoEmbed(url = url, label = label))
                i++
                continue
            }

            // Blockquote > ...
            if (trimmed.startsWith(">")) {
                val quoteLines = mutableListOf<List<InlineSpan>>()
                while (i < lines.size && lines[i].trim().startsWith(">")) {
                    val content = lines[i].trim().removePrefix(">").trim()
                    if (content.isNotEmpty()) {
                        quoteLines.add(parseInlines(content))
                    }
                    i++
                }
                if (quoteLines.isNotEmpty()) {
                    blocks.add(MarkdownBlock.Blockquote(quoteLines))
                }
                continue
            }

            // GFM Table: header | separator | rows
            if ('|' in trimmed && i + 1 < lines.size && TABLE_SEP_RE.matches(lines[i + 1].trim())) {
                val headers = splitTableCells(trimmed).map { parseInlines(it) }
                i += 2
                val rows = mutableListOf<List<List<InlineSpan>>>()
                while (i < lines.size && lines[i].trim().isNotEmpty() && '|' in lines[i]) {
                    rows.add(splitTableCells(lines[i].trim()).map { parseInlines(it) })
                    i++
                }
                blocks.add(MarkdownBlock.Table(headers = headers, rows = rows))
                continue
            }

            // Task list - [ ] / - [x]
            if (TASK_ITEM_RE.matches(trimmed)) {
                val tasks = mutableListOf<TaskListItem>()
                while (i < lines.size) {
                    val m = TASK_ITEM_RE.matchEntire(lines[i].trim()) ?: break
                    val checked = m.groupValues[1].equals("x", ignoreCase = true)
                    tasks.add(TaskListItem(checked = checked, inlines = parseInlines(m.groupValues[2].trim())))
                    i++
                }
                blocks.add(MarkdownBlock.TaskList(tasks))
                continue
            }

            // Bullet list - / * / +
            if (BULLET_ITEM_RE.matches(trimmed)) {
                val items = mutableListOf<List<InlineSpan>>()
                while (i < lines.size) {
                    val m = BULLET_ITEM_RE.matchEntire(lines[i].trim()) ?: break
                    items.add(parseInlines(m.groupValues[1].trim()))
                    i++
                }
                blocks.add(MarkdownBlock.BulletList(items))
                continue
            }

            // Ordered list 1. / 2.
            val ordStart = ORDERED_ITEM_RE.matchEntire(trimmed)
            if (ordStart != null) {
                val startNum = ordStart.groupValues[1].toIntOrNull() ?: 1
                val items = mutableListOf<List<InlineSpan>>()
                while (i < lines.size) {
                    val m = ORDERED_ITEM_RE.matchEntire(lines[i].trim()) ?: break
                    items.add(parseInlines(m.groupValues[2].trim()))
                    i++
                }
                blocks.add(MarkdownBlock.OrderedList(start = startNum, items = items))
                continue
            }

            // Paragraph (collect contiguous plain lines, splitting out any embedded markdown images)
            val paraLines = mutableListOf<String>()
            while (i < lines.size) {
                val cur = lines[i].trim()
                if (cur.isEmpty() ||
                    cur.startsWith("```") ||
                    cur.startsWith("~~~") ||
                    cur.startsWith(">") ||
                    HEADING_RE.matches(cur) ||
                    DIVIDER_RE.matches(cur) ||
                    STANDALONE_IMAGE_RE.matches(cur) ||
                    VIDEO_RE.matches(cur) ||
                    TASK_ITEM_RE.matches(cur) ||
                    BULLET_ITEM_RE.matches(cur) ||
                    ORDERED_ITEM_RE.matches(cur) ||
                    ('|' in cur && i + 1 < lines.size && TABLE_SEP_RE.matches(lines[i + 1].trim()))
                ) {
                    break
                }
                paraLines.add(cur)
                i++
            }
            val joined = paraLines.joinToString("\n")
            // Check if paragraph contains inline markdown images and extract them cleanly
            val inlineImgMatches = INLINE_IMAGE_RE.findAll(joined).toList()
            if (inlineImgMatches.isNotEmpty()) {
                var cursor = 0
                inlineImgMatches.forEach { m ->
                    val before = joined.substring(cursor, m.range.first).trim()
                    if (before.isNotEmpty()) blocks.add(MarkdownBlock.Paragraph(parseInlines(before)))
                    val alt = m.groupValues[1].trim()
                    val rawUrl = m.groupValues[2].trim().substringBefore(' ')
                    val resolved = resolveUrl(rawUrl)
                    images.add(resolved)
                    blocks.add(MarkdownBlock.Image(url = resolved, alt = alt, rawUrl = rawUrl))
                    cursor = m.range.last + 1
                }
                val tail = joined.substring(cursor).trim()
                if (tail.isNotEmpty()) blocks.add(MarkdownBlock.Paragraph(parseInlines(tail)))
            } else if (joined.isNotBlank()) {
                blocks.add(MarkdownBlock.Paragraph(parseInlines(joined)))
            }
        }

        return MarkdownDocument(blocks = blocks, toc = toc, imageUrls = images)
    }

    fun parseInlines(input: String): List<InlineSpan> {
        if (input.isEmpty()) return emptyList()
        val spans = mutableListOf<InlineSpan>()
        var idx = 0
        val textBuf = StringBuilder()

        fun flushText() {
            if (textBuf.isNotEmpty()) {
                spans.add(InlineSpan.Text(textBuf.toString()))
                textBuf.clear()
            }
        }

        while (idx < input.length) {
            val rest = input.substring(idx)

            // Escaped punctuation \* \_ \`
            if (input[idx] == '\\' && idx + 1 < input.length) {
                textBuf.append(input[idx + 1])
                idx += 2
                continue
            }

            // Inline code `code`
            if (input[idx] == '`') {
                val end = input.indexOf('`', idx + 1)
                if (end > idx) {
                    flushText()
                    spans.add(InlineSpan.InlineCode(input.substring(idx + 1, end)))
                    idx = end + 1
                    continue
                }
            }

            // Link [label](url)
            if (input[idx] == '[') {
                val linkMatch = LINK_PREFIX_RE.find(rest)
                if (linkMatch != null) {
                    flushText()
                    spans.add(InlineSpan.Link(label = linkMatch.groupValues[1], url = linkMatch.groupValues[2].trim()))
                    idx += linkMatch.value.length
                    continue
                }
            }

            // Highlight ==text==
            if (rest.startsWith("==")) {
                val end = input.indexOf("==", idx + 2)
                if (end > idx + 2) {
                    flushText()
                    spans.add(InlineSpan.Highlight(input.substring(idx + 2, end)))
                    idx = end + 2
                    continue
                }
            }

            // Bold **text** or __text__
            if (rest.startsWith("**") || rest.startsWith("__")) {
                val marker = rest.take(2)
                val end = input.indexOf(marker, idx + 2)
                if (end > idx + 2) {
                    flushText()
                    spans.add(InlineSpan.Bold(parseInlines(input.substring(idx + 2, end))))
                    idx = end + 2
                    continue
                }
            }

            // Strike ~~text~~
            if (rest.startsWith("~~")) {
                val end = input.indexOf("~~", idx + 2)
                if (end > idx + 2) {
                    flushText()
                    spans.add(InlineSpan.Strike(parseInlines(input.substring(idx + 2, end))))
                    idx = end + 2
                    continue
                }
            }

            // Italic *text* or _text_
            if ((input[idx] == '*' || input[idx] == '_') && (idx + 1 < input.length && !input[idx + 1].isWhitespace())) {
                val marker = input[idx]
                val end = input.indexOf(marker, idx + 1)
                if (end > idx + 1 && !input[end - 1].isWhitespace()) {
                    flushText()
                    spans.add(InlineSpan.Italic(parseInlines(input.substring(idx + 1, end))))
                    idx = end + 1
                    continue
                }
            }

            // @mention
            if (input[idx] == '@' && (idx == 0 || input[idx - 1].isWhitespace() || input[idx - 1] in "（([，,：:")) {
                val m = MENTION_PREFIX_RE.find(rest)
                if (m != null) {
                    flushText()
                    spans.add(InlineSpan.Mention(m.groupValues[1]))
                    idx += m.value.length
                    continue
                }
            }

            // Bare URL https://...
            if (rest.startsWith("https://") || rest.startsWith("http://")) {
                val urlMatch = BARE_URL_PREFIX_RE.find(rest)
                if (urlMatch != null) {
                    flushText()
                    val url = urlMatch.value.trimEnd('.', ',', ';', ')', ']', '。', '，')
                    spans.add(InlineSpan.Link(label = url, url = url))
                    idx += url.length
                    continue
                }
            }

            textBuf.append(input[idx])
            idx++
        }
        flushText()
        return spans
    }

    fun extractPlainText(spans: List<InlineSpan>): String = buildString {
        spans.forEach { span ->
            when (span) {
                is InlineSpan.Text -> append(span.text)
                is InlineSpan.Bold -> append(extractPlainText(span.children))
                is InlineSpan.Italic -> append(extractPlainText(span.children))
                is InlineSpan.Strike -> append(extractPlainText(span.children))
                is InlineSpan.InlineCode -> append(span.code)
                is InlineSpan.Highlight -> append(span.text)
                is InlineSpan.Link -> append(span.label)
                is InlineSpan.Mention -> append("@${span.handle}")
            }
        }
    }

    private fun looksLikeHtml(source: String): Boolean {
        val trimmed = source.trimStart()
        return trimmed.startsWith("<p") ||
            trimmed.startsWith("<h1") ||
            trimmed.startsWith("<h2") ||
            trimmed.startsWith("<div") ||
            trimmed.startsWith("<article") ||
            trimmed.startsWith("<figure")
    }

    /**
     * Converts legacy / rich-text HTML posts from BBS into clean Markdown before AST parsing.
     */
    fun htmlToMarkdown(html: String): String {
        var s = html
            .replace("\r\n", "\n")
            .replace(Regex("(?is)<script[^>]*>.*?</script>"), "")
            .replace(Regex("(?is)<style[^>]*>.*?</style>"), "")

        // Code blocks
        s = s.replace(Regex("(?is)<pre[^>]*>\\s*<code(?:[^>]*class=[\"'][^\"']*language-([a-zA-Z0-9_+-]+)[^\"']*[\"'])?[^>]*>(.*?)</code>\\s*</pre>")) { m ->
            val lang = m.groupValues[1]
            val code = unescapeHtml(m.groupValues[2]).trim('\n')
            "\n```$lang\n$code\n```\n"
        }

        // Headings
        for (lvl in 1..4) {
            val prefix = "#".repeat(lvl)
            s = s.replace(Regex("(?is)<h$lvl[^>]*>(.*?)</h$lvl>")) { m ->
                "\n$prefix ${stripTags(m.groupValues[1]).trim()}\n"
            }
        }

        // Images
        s = s.replace(Regex("(?is)<img[^>]*src=[\"']([^\"']+)[\"'][^>]*>")) { m ->
            val tag = m.value
            val src = m.groupValues[1]
            val alt = Regex("alt=[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(tag)?.groupValues?.get(1).orEmpty()
            "\n![${unescapeHtml(alt)}]($src)\n"
        }

        // Video / iframe embeds
        s = s.replace(Regex("(?is)<(?:video|iframe)[^>]*src=[\"']([^\"']+)[\"'][^>]*>.*?</(?:video|iframe)>")) { m ->
            "\n@[视频](${m.groupValues[1]})\n"
        }

        // Blockquotes
        s = s.replace(Regex("(?is)<blockquote[^>]*>(.*?)</blockquote>")) { m ->
            val lines = stripTags(m.groupValues[1]).trim().split('\n').filter { it.isNotBlank() }
            "\n" + lines.joinToString("\n") { "> ${it.trim()}" } + "\n"
        }

        // Inline formatting
        s = s.replace(Regex("(?is)<(?:strong|b)[^>]*>(.*?)</(?:strong|b)>")) { "**${it.groupValues[1]}**" }
        s = s.replace(Regex("(?is)<(?:em|i)[^>]*>(.*?)</(?:em|i)>")) { "*${it.groupValues[1]}*" }
        s = s.replace(Regex("(?is)<(?:del|s)[^>]*>(.*?)</(?:del|s)>")) { "~~${it.groupValues[1]}~~" }
        s = s.replace(Regex("(?is)<mark[^>]*>(.*?)</mark>")) { "==${it.groupValues[1]}==" }
        s = s.replace(Regex("(?is)<code[^>]*>(.*?)</code>")) { "`${unescapeHtml(it.groupValues[1])}`" }
        s = s.replace(Regex("(?is)<a[^>]*href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>")) { m ->
            "[${stripTags(m.groupValues[2]).trim()}](${m.groupValues[1]})"
        }

        // Lists
        s = s.replace(Regex("(?is)<li[^>]*>(.*?)</li>")) { m -> "\n- ${stripTags(m.groupValues[1]).trim()}" }

        // Tables (simple extraction)
        s = s.replace(Regex("(?is)<table[^>]*>(.*?)</table>")) { tableMatch ->
            val rows = Regex("(?is)<tr[^>]*>(.*?)</tr>").findAll(tableMatch.groupValues[1]).map { tr ->
                Regex("(?is)<t[hd][^>]*>(.*?)</t[hd]>").findAll(tr.groupValues[1])
                    .map { stripTags(it.groupValues[1]).trim().replace("\n", " ") }
                    .toList()
            }.filter { it.isNotEmpty() }.toList()
            if (rows.isEmpty()) "" else buildString {
                append('\n')
                append("| ").append(rows.first().joinToString(" | ")).append(" |\n")
                append("| ").append(rows.first().joinToString(" | ") { "---" }).append(" |\n")
                rows.drop(1).forEach { r ->
                    append("| ").append(r.joinToString(" | ")).append(" |\n")
                }
            }
        }

        // Dividers & line breaks & paragraphs
        s = s.replace(Regex("(?is)<hr[^>]*/?>"), "\n---\n")
        s = s.replace(Regex("(?is)<br\\s*/?>"), "\n")
        s = s.replace(Regex("(?is)</(?:p|div|section|article|figure|ul|ol)>"), "\n\n")
        s = stripTags(s)
        return unescapeHtml(s).replace(Regex("\n{3,}"), "\n\n").trim()
    }

    private fun stripTags(text: String): String = text.replace(Regex("<[^>]+>"), "")

    private fun unescapeHtml(text: String): String = text
        .replace("&nbsp;", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&#39;", "'")

    private fun splitTableCells(line: String): List<String> =
        line.trim().removePrefix("|").removeSuffix("|").split('|').map { it.trim() }

    private val HEADING_RE = Regex("^(#{1,6})\\s+(.+)$")
    private val DIVIDER_RE = Regex("^(?:(?:-\\s*){3,}|(?:\\*\\s*){3,}|(?:_\\s*){3,})$")
    private val STANDALONE_IMAGE_RE = Regex("^!\\[([^\\]]*)]\\(([^)]+)\\)$")
    private val INLINE_IMAGE_RE = Regex("!\\[([^\\]]*)]\\(([^)]+)\\)")
    private val VIDEO_RE = Regex("^@\\[([^\\]]*)]\\(([^)]+)\\)$")
    private val TASK_ITEM_RE = Regex("^[-*+]\\s+\\[([ xX])]\\s+(.+)$")
    private val BULLET_ITEM_RE = Regex("^[-*+]\\s+(.+)$")
    private val ORDERED_ITEM_RE = Regex("^(\\d+)\\.\\s+(.+)$")
    private val TABLE_SEP_RE = Regex("^\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)*\\|?$")
    private val LINK_PREFIX_RE = Regex("^\\[([^\\]]+)]\\(([^)]+)\\)")
    private val MENTION_PREFIX_RE = Regex("^@([A-Za-z0-9_\\u4e00-\\u9fff-]{1,32})")
    private val BARE_URL_PREFIX_RE = Regex("^https?://[^\\s<>\"'\\)]+")
}
