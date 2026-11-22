package net.igng.bbs.core.markdown

import java.util.UUID

enum class WysiwygBlockType(val label: String) {
    HEADING_1("一级标题"),
    HEADING_2("二级标题"),
    HEADING_3("三级标题"),
    HEADING_4("四级标题"),
    PARAGRAPH("正文段落"),
    BULLET_LIST("无序列表"),
    ORDERED_LIST("有序列表"),
    TASK_LIST("待办清单"),
    BLOCKQUOTE("引用块"),
    CODE_BLOCK("代码块"),
    TABLE("表格"),
    DIVIDER("分割线"),
    IMAGE("图片"),
    VIDEO("视频链接"),
}

data class WysiwygTaskItem(
    val text: String = "",
    val checked: Boolean = false,
)

data class WysiwygBlock(
    val id: String = newBlockId(),
    val type: WysiwygBlockType = WysiwygBlockType.PARAGRAPH,
    val text: String = "",
    val listItems: List<String> = emptyList(),
    val taskItems: List<WysiwygTaskItem> = emptyList(),
    val language: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val tableHeaders: List<String> = listOf("项目", "说明"),
    val tableRows: List<List<String>> = listOf(listOf("", "")),
) {
    companion object {
        fun newBlockId(): String = "blk_${UUID.randomUUID().toString().replace("-", "").take(10)}"

        fun emptyOf(type: WysiwygBlockType): WysiwygBlock = when (type) {
            WysiwygBlockType.BULLET_LIST, WysiwygBlockType.ORDERED_LIST ->
                WysiwygBlock(type = type, listItems = listOf(""))
            WysiwygBlockType.TASK_LIST ->
                WysiwygBlock(type = type, taskItems = listOf(WysiwygTaskItem("", false)))
            WysiwygBlockType.CODE_BLOCK ->
                WysiwygBlock(type = type, language = "kotlin", text = "")
            WysiwygBlockType.TABLE ->
                WysiwygBlock(type = type, tableHeaders = listOf("列 1", "列 2"), tableRows = listOf(listOf("", "")))
            WysiwygBlockType.DIVIDER ->
                WysiwygBlock(type = type)
            WysiwygBlockType.IMAGE ->
                WysiwygBlock(type = type, imageUrl = "", caption = "")
            WysiwygBlockType.VIDEO ->
                WysiwygBlock(type = type, imageUrl = "", caption = "视频")
            else ->
                WysiwygBlock(type = type, text = "")
        }
    }
}

/**
 * Lossless bidirectional converter between IGNG BBS Markdown/HTML and the native block-based
 * WYSIWYG document model.
 */
object WysiwygCodec {

    fun fromMarkdown(source: String, format: String = "markdown"): List<WysiwygBlock> {
        val trimmed = source.trim()
        if (trimmed.isEmpty()) {
            return listOf(WysiwygBlock(type = WysiwygBlockType.PARAGRAPH, text = ""))
        }
        val doc = MarkdownParser.parse(trimmed, format = format)
        val out = doc.blocks.map { block ->
            when (block) {
                is MarkdownBlock.Heading -> WysiwygBlock(
                    type = when (block.level) {
                        1 -> WysiwygBlockType.HEADING_1
                        2 -> WysiwygBlockType.HEADING_2
                        3 -> WysiwygBlockType.HEADING_3
                        else -> WysiwygBlockType.HEADING_4
                    },
                    text = serializeInlines(block.inlines),
                )
                is MarkdownBlock.Paragraph -> WysiwygBlock(
                    type = WysiwygBlockType.PARAGRAPH,
                    text = serializeInlines(block.inlines),
                )
                is MarkdownBlock.BulletList -> WysiwygBlock(
                    type = WysiwygBlockType.BULLET_LIST,
                    listItems = block.items.map(::serializeInlines),
                )
                is MarkdownBlock.OrderedList -> WysiwygBlock(
                    type = WysiwygBlockType.ORDERED_LIST,
                    listItems = block.items.map(::serializeInlines),
                )
                is MarkdownBlock.TaskList -> WysiwygBlock(
                    type = WysiwygBlockType.TASK_LIST,
                    taskItems = block.items.map { WysiwygTaskItem(serializeInlines(it.inlines), it.checked) },
                )
                is MarkdownBlock.Blockquote -> WysiwygBlock(
                    type = WysiwygBlockType.BLOCKQUOTE,
                    text = block.lines.joinToString("\n") { serializeInlines(it) },
                )
                is MarkdownBlock.CodeBlock -> WysiwygBlock(
                    type = WysiwygBlockType.CODE_BLOCK,
                    language = block.language,
                    text = block.code,
                )
                is MarkdownBlock.Table -> WysiwygBlock(
                    type = WysiwygBlockType.TABLE,
                    tableHeaders = block.headers.map(::serializeInlines),
                    tableRows = block.rows.map { r -> r.map(::serializeInlines) },
                )
                is MarkdownBlock.Divider -> WysiwygBlock(type = WysiwygBlockType.DIVIDER)
                is MarkdownBlock.Image -> WysiwygBlock(
                    type = WysiwygBlockType.IMAGE,
                    imageUrl = block.rawUrl.ifBlank { block.url },
                    caption = block.alt,
                )
                is MarkdownBlock.VideoEmbed -> WysiwygBlock(
                    type = WysiwygBlockType.VIDEO,
                    imageUrl = block.url,
                    caption = block.label,
                )
            }
        }
        return out.ifEmpty { listOf(WysiwygBlock(type = WysiwygBlockType.PARAGRAPH, text = "")) }
    }

    fun toMarkdown(blocks: List<WysiwygBlock>): String = blocks.mapNotNull { block ->
        when (block.type) {
            WysiwygBlockType.HEADING_1 -> block.text.trim().takeIf { it.isNotEmpty() }?.let { "# $it" }
            WysiwygBlockType.HEADING_2 -> block.text.trim().takeIf { it.isNotEmpty() }?.let { "## $it" }
            WysiwygBlockType.HEADING_3 -> block.text.trim().takeIf { it.isNotEmpty() }?.let { "### $it" }
            WysiwygBlockType.HEADING_4 -> block.text.trim().takeIf { it.isNotEmpty() }?.let { "#### $it" }
            WysiwygBlockType.PARAGRAPH -> block.text.trim().takeIf { it.isNotEmpty() }
            WysiwygBlockType.BULLET_LIST -> {
                val items = block.listItems.map { it.trim() }.filter { it.isNotEmpty() }
                items.takeIf { it.isNotEmpty() }?.joinToString("\n") { "- $it" }
            }
            WysiwygBlockType.ORDERED_LIST -> {
                val items = block.listItems.map { it.trim() }.filter { it.isNotEmpty() }
                items.takeIf { it.isNotEmpty() }?.mapIndexed { idx, item -> "${idx + 1}. $item" }?.joinToString("\n")
            }
            WysiwygBlockType.TASK_LIST -> {
                val items = block.taskItems.filter { it.text.trim().isNotEmpty() }
                items.takeIf { it.isNotEmpty() }?.joinToString("\n") { item ->
                    val mark = if (item.checked) "x" else " "
                    "- [$mark] ${item.text.trim()}"
                }
            }
            WysiwygBlockType.BLOCKQUOTE -> {
                val lines = block.text.split('\n').map { it.trim() }.filter { it.isNotEmpty() }
                lines.takeIf { it.isNotEmpty() }?.joinToString("\n") { "> $it" }
            }
            WysiwygBlockType.CODE_BLOCK -> {
                val code = block.text.trimEnd('\n')
                if (code.isBlank()) null else "```${block.language.trim()}\n$code\n```"
            }
            WysiwygBlockType.TABLE -> {
                val cols = block.tableHeaders.size.coerceAtLeast(1)
                val headerRow = "| " + block.tableHeaders.joinToString(" | ") { it.replace("|", "\\|").ifBlank { " " } } + " |"
                val sepRow = "| " + List(cols) { "---" }.joinToString(" | ") + " |"
                val bodyRows = block.tableRows.map { row ->
                    val padded = List(cols) { c -> row.getOrElse(c) { "" }.replace("|", "\\|").ifBlank { " " } }
                    "| " + padded.joinToString(" | ") + " |"
                }
                (listOf(headerRow, sepRow) + bodyRows).joinToString("\n")
            }
            WysiwygBlockType.DIVIDER -> "---"
            WysiwygBlockType.IMAGE -> {
                val url = block.imageUrl.trim()
                if (url.isEmpty()) null else "![${block.caption.trim()}]($url)"
            }
            WysiwygBlockType.VIDEO -> {
                val url = block.imageUrl.trim()
                if (url.isEmpty()) null else "@[${block.caption.trim().ifBlank { "视频" }}]($url)"
            }
        }
    }.joinToString("\n\n")

    fun convertBlockType(block: WysiwygBlock, target: WysiwygBlockType): WysiwygBlock {
        if (block.type == target) return block
        val plain = when (block.type) {
            WysiwygBlockType.BULLET_LIST, WysiwygBlockType.ORDERED_LIST -> block.listItems.joinToString("\n")
            WysiwygBlockType.TASK_LIST -> block.taskItems.joinToString("\n") { it.text }
            WysiwygBlockType.IMAGE, WysiwygBlockType.VIDEO -> block.caption
            else -> block.text
        }
        return when (target) {
            WysiwygBlockType.BULLET_LIST, WysiwygBlockType.ORDERED_LIST ->
                block.copy(type = target, listItems = plain.split('\n').ifEmpty { listOf("") })
            WysiwygBlockType.TASK_LIST ->
                block.copy(type = target, taskItems = plain.split('\n').map { WysiwygTaskItem(it, false) }.ifEmpty { listOf(WysiwygTaskItem()) })
            WysiwygBlockType.DIVIDER ->
                block.copy(type = target)
            WysiwygBlockType.TABLE ->
                block.copy(type = target, tableHeaders = listOf("项目", "说明"), tableRows = listOf(listOf(plain, "")))
            WysiwygBlockType.CODE_BLOCK ->
                block.copy(type = target, text = plain, language = block.language.ifBlank { "kotlin" })
            else ->
                block.copy(type = target, text = plain)
        }
    }

    fun applyInlineWrap(
        current: String,
        prefix: String,
        suffix: String = prefix,
        placeholder: String = "文本",
    ): String {
        val trimmed = current.trimEnd()
        return if (trimmed.isEmpty()) "$prefix$placeholder$suffix" else "$current $prefix$placeholder$suffix"
    }

    private fun serializeInlines(spans: List<InlineSpan>): String = buildString {
        spans.forEach { span ->
            when (span) {
                is InlineSpan.Text -> append(span.text)
                is InlineSpan.Bold -> append("**").append(serializeInlines(span.children)).append("**")
                is InlineSpan.Italic -> append("*").append(serializeInlines(span.children)).append("*")
                is InlineSpan.Strike -> append("~~").append(serializeInlines(span.children)).append("~~")
                is InlineSpan.InlineCode -> append("`").append(span.code).append("`")
                is InlineSpan.Highlight -> append("==").append(span.text).append("==")
                is InlineSpan.Link -> append("[").append(span.label).append("](").append(span.url).append(")")
                is InlineSpan.Mention -> append("@").append(span.handle)
            }
        }
    }
}

/**
 * Bounded immutable undo/redo stack for block editor state.
 */
data class WysiwygHistory(
    val undoStack: List<List<WysiwygBlock>> = emptyList(),
    val present: List<WysiwygBlock> = listOf(WysiwygBlock(type = WysiwygBlockType.PARAGRAPH)),
    val redoStack: List<List<WysiwygBlock>> = emptyList(),
) {
    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun commit(next: List<WysiwygBlock>, maxDepth: Int = 40): WysiwygHistory {
        if (next == present) return this
        val nextUndo = (undoStack + listOf(present)).takeLast(maxDepth)
        return WysiwygHistory(undoStack = nextUndo, present = next, redoStack = emptyList())
    }

    fun undo(): WysiwygHistory {
        if (undoStack.isEmpty()) return this
        val previous = undoStack.last()
        return WysiwygHistory(
            undoStack = undoStack.dropLast(1),
            present = previous,
            redoStack = listOf(present) + redoStack,
        )
    }

    fun redo(): WysiwygHistory {
        if (redoStack.isEmpty()) return this
        val next = redoStack.first()
        return WysiwygHistory(
            undoStack = undoStack + listOf(present),
            present = next,
            redoStack = redoStack.drop(1),
        )
    }
}
