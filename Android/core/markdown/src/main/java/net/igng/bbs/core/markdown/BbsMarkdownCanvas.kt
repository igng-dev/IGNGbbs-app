package net.igng.bbs.core.markdown

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import net.igng.bbs.core.designsystem.IgngColors
import net.igng.bbs.core.designsystem.IgngRadius
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.igngPressScale
import net.igng.bbs.core.designsystem.readerSurfaceColors
import net.igng.bbs.core.model.ReaderTypographyPrefs

@Composable
fun BbsMarkdownDocumentView(
    document: MarkdownDocument,
    typography: ReaderTypographyPrefs,
    onImageClick: (String) -> Unit,
    onLinkClick: (String) -> Unit,
    onMentionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        document.blocks.forEach { block ->
            BbsMarkdownBlockView(
                block = block,
                typography = typography,
                onImageClick = onImageClick,
                onLinkClick = onLinkClick,
                onMentionClick = onMentionClick,
            )
        }
    }
}

@Composable
fun BbsMarkdownBlockView(
    block: MarkdownBlock,
    typography: ReaderTypographyPrefs,
    onImageClick: (String) -> Unit,
    onLinkClick: (String) -> Unit,
    onMentionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    val (_, primaryText, secondaryText) = colors.readerSurfaceColors(typography.canvasTone)
    val fontFamily = if (typography.preferSerif) FontFamily.Serif else FontFamily.SansSerif
    val bodySize = typography.fontSizeSp.sp
    val bodyLineHeight = (typography.fontSizeSp * typography.lineHeightMultiplier).sp
    val baseBodyStyle = TextStyle(
        fontFamily = fontFamily,
        fontSize = bodySize,
        lineHeight = bodyLineHeight,
        color = primaryText,
    )

    when (block) {
        is MarkdownBlock.Heading -> {
            val scale = when (block.level) {
                1 -> 1.48f
                2 -> 1.30f
                3 -> 1.15f
                else -> 1.05f
            }
            val topSpace = when (block.level) {
                1 -> 10.dp
                2 -> 8.dp
                else -> 4.dp
            }
            val annotated = remember(block.inlines, colors) {
                buildInlineAnnotatedString(block.inlines, colors)
            }
            Column(modifier = modifier.padding(top = topSpace)) {
                InlineClickableText(
                    annotated = annotated,
                    style = baseBodyStyle.copy(
                        fontSize = (typography.fontSizeSp * scale).sp,
                        lineHeight = (typography.fontSizeSp * scale * 1.35f).sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    onLinkClick = onLinkClick,
                    onMentionClick = onMentionClick,
                )
                if (block.level <= 2) {
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = colors.line)
                }
            }
        }

        is MarkdownBlock.Paragraph -> {
            val annotated = remember(block.inlines, colors) {
                buildInlineAnnotatedString(block.inlines, colors)
            }
            InlineClickableText(
                annotated = annotated,
                style = baseBodyStyle,
                onLinkClick = onLinkClick,
                onMentionClick = onMentionClick,
                modifier = modifier.fillMaxWidth(),
            )
        }

        is MarkdownBlock.BulletList -> {
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                block.items.forEach { item ->
                    val annotated = remember(item, colors) { buildInlineAnnotatedString(item, colors) }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text(
                            text = "•",
                            style = baseBodyStyle.copy(color = colors.accent, fontWeight = FontWeight.Bold),
                            modifier = Modifier.width(18.dp),
                        )
                        InlineClickableText(
                            annotated = annotated,
                            style = baseBodyStyle,
                            onLinkClick = onLinkClick,
                            onMentionClick = onMentionClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        is MarkdownBlock.OrderedList -> {
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                block.items.forEachIndexed { idx, item ->
                    val annotated = remember(item, colors) { buildInlineAnnotatedString(item, colors) }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${block.start + idx}.",
                            style = baseBodyStyle.copy(
                                color = colors.accent,
                                fontWeight = FontWeight.SemiBold,
                                fontFeatureSettings = "tnum",
                            ),
                            modifier = Modifier.widthIn(min = 24.dp),
                        )
                        InlineClickableText(
                            annotated = annotated,
                            style = baseBodyStyle,
                            onLinkClick = onLinkClick,
                            onMentionClick = onMentionClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        is MarkdownBlock.TaskList -> {
            Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                block.items.forEach { item ->
                    val annotated = remember(item.inlines, colors) { buildInlineAnnotatedString(item.inlines, colors) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = if (item.checked) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = if (item.checked) colors.success else colors.textMuted,
                            modifier = Modifier
                                .padding(top = 3.dp)
                                .size(18.dp),
                        )
                        InlineClickableText(
                            annotated = annotated,
                            style = baseBodyStyle.copy(
                                color = if (item.checked) secondaryText else primaryText,
                                textDecoration = if (item.checked) TextDecoration.LineThrough else TextDecoration.None,
                            ),
                            onLinkClick = onLinkClick,
                            onMentionClick = onMentionClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        is MarkdownBlock.Blockquote -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(IgngRadius.control))
                    .background(colors.tileSoft)
                    .padding(end = 12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(colors.accent.copy(alpha = 0.7f)),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    block.lines.forEach { line ->
                        val annotated = remember(line, colors) { buildInlineAnnotatedString(line, colors) }
                        InlineClickableText(
                            annotated = annotated,
                            style = baseBodyStyle.copy(color = secondaryText),
                            onLinkClick = onLinkClick,
                            onMentionClick = onMentionClick,
                        )
                    }
                }
            }
        }

        is MarkdownBlock.CodeBlock -> {
            val context = LocalContext.current
            var copied by remember(block.code) { mutableStateOf(false) }
            val shape = RoundedCornerShape(IgngRadius.card)
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.codeBackground)
                    .border(1.dp, colors.line, shape),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.tileSoft)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = block.language.ifBlank { "代码" },
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                        color = colors.textSecondary,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(IgngRadius.pill))
                            .clickable {
                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                cm?.setPrimaryClip(ClipData.newPlainText("code", block.code))
                                copied = true
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "复制代码",
                            tint = colors.accent,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (copied) "已复制" else "复制",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accent,
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(14.dp),
                ) {
                    Text(
                        text = block.code,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = (typography.fontSizeSp - 2.5f).coerceAtLeast(12f).sp,
                            lineHeight = (typography.fontSizeSp * 1.35f).sp,
                            color = colors.textPrimary,
                        ),
                    )
                }
            }
        }

        is MarkdownBlock.Table -> {
            val shape = RoundedCornerShape(IgngRadius.card)
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .border(1.dp, colors.line, shape)
                    .horizontalScroll(rememberScrollState()),
            ) {
                Column {
                    Row(modifier = Modifier.background(colors.tileSoft)) {
                        block.headers.forEach { cell ->
                            val annotated = remember(cell, colors) { buildInlineAnnotatedString(cell, colors) }
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 104.dp, max = 220.dp)
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                            ) {
                                Text(
                                    text = annotated,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = primaryText,
                                    ),
                                )
                            }
                        }
                    }
                    block.rows.forEachIndexed { rIdx, row ->
                        HorizontalDivider(color = colors.line)
                        Row(modifier = Modifier.background(if (rIdx % 2 == 0) colors.tile else colors.tileSoft.copy(alpha = 0.5f))) {
                            val cols = block.headers.size.coerceAtLeast(row.size)
                            for (c in 0 until cols) {
                                val cell = row.getOrNull(c).orEmpty()
                                val annotated = remember(cell, colors) { buildInlineAnnotatedString(cell, colors) }
                                Box(
                                    modifier = Modifier
                                        .widthIn(min = 104.dp, max = 220.dp)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = annotated,
                                        style = MaterialTheme.typography.bodySmall.copy(color = primaryText),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        is MarkdownBlock.Divider -> {
            HorizontalDivider(
                modifier = modifier.padding(vertical = 8.dp),
                color = colors.lineStrong,
            )
        }

        is MarkdownBlock.Image -> {
            val shape = RoundedCornerShape(IgngRadius.card)
            Column(
                modifier = modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AsyncImage(
                    model = block.url,
                    contentDescription = block.alt.ifBlank { "文章配图" },
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(colors.tileSoft)
                        .border(1.dp, colors.line, shape)
                        .igngPressScale(onClick = { onImageClick(block.url) }),
                )
                if (block.alt.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = block.alt,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText,
                    )
                }
            }
        }

        is MarkdownBlock.VideoEmbed -> {
            val shape = RoundedCornerShape(IgngRadius.card)
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(colors.tileSoft)
                    .border(1.dp, colors.line, shape)
                    .igngPressScale(onClick = { onLinkClick(block.url) })
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayCircleOutline,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(30.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = block.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = primaryText,
                    )
                    Text(
                        text = block.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryText,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
@Composable
private fun InlineClickableText(
    annotated: AnnotatedString,
    style: TextStyle,
    onLinkClick: (String) -> Unit,
    onMentionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ClickableText(
        text = annotated,
        style = style,
        modifier = modifier,
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()?.let {
                onLinkClick(it.item)
                return@ClickableText
            }
            annotated.getStringAnnotations(tag = "MENTION", start = offset, end = offset).firstOrNull()?.let {
                onMentionClick(it.item)
            }
        },
    )
}

private fun buildInlineAnnotatedString(spans: List<InlineSpan>, colors: IgngColors): AnnotatedString =
    buildAnnotatedString {
        fun appendSpans(list: List<InlineSpan>) {
            list.forEach { span ->
                when (span) {
                    is InlineSpan.Text -> append(span.text)
                    is InlineSpan.Bold -> {
                        val start = length
                        appendSpans(span.children)
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
                    }
                    is InlineSpan.Italic -> {
                        val start = length
                        appendSpans(span.children)
                        addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length)
                    }
                    is InlineSpan.Strike -> {
                        val start = length
                        appendSpans(span.children)
                        addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, length)
                    }
                    is InlineSpan.InlineCode -> {
                        val start = length
                        append(" ${span.code} ")
                        addStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = colors.codeBackground,
                                color = colors.accent,
                            ),
                            start,
                            length,
                        )
                    }
                    is InlineSpan.Highlight -> {
                        val start = length
                        append(span.text)
                        addStyle(SpanStyle(background = colors.warningLow), start, length)
                    }
                    is InlineSpan.Link -> {
                        val start = length
                        append(span.label)
                        addStyle(
                            SpanStyle(
                                color = colors.accent,
                                textDecoration = TextDecoration.Underline,
                            ),
                            start,
                            length,
                        )
                        addStringAnnotation("URL", span.url, start, length)
                    }
                    is InlineSpan.Mention -> {
                        val start = length
                        append("@${span.handle}")
                        addStyle(
                            SpanStyle(
                                color = colors.accent,
                                fontWeight = FontWeight.Medium,
                            ),
                            start,
                            length,
                        )
                        addStringAnnotation("MENTION", span.handle, start, length)
                    }
                }
            }
        }
        appendSpans(spans)
    }
