package net.igng.bbs.core.markdown

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownAndWysiwygTest {

    @Test
    fun parsesRichMarkdownBlocksTocAndImages() {
        val md = """
            # 一级标题

            正文支持 **加粗**、*斜体*、`行内代码`、==高亮== 与 [链接](https://www.igngbbs.net) 以及 @alice。

            ## 二级小节

            - 第一项
            - 第二项

            - [x] 已完成任务
            - [ ] 待办任务

            > 一段优雅的引用

            ```kotlin
            val ok = true
            ```

            | 列1 | 列2 |
            | --- | --- |
            | A | B |

            ![配图说明](/api/media/files/0123456789abcdef0123456789abcdef/content)

            ---
        """.trimIndent()

        val doc = MarkdownParser.parse(md)
        assertEquals(2, doc.toc.size)
        assertEquals("一级标题", doc.toc[0].title)
        assertEquals("二级小节", doc.toc[1].title)
        assertEquals(1, doc.imageUrls.size)
        assertTrue(doc.blocks.any { it is MarkdownBlock.CodeBlock && it.language == "kotlin" })
        assertTrue(doc.blocks.any { it is MarkdownBlock.Table })
        assertTrue(doc.blocks.any { it is MarkdownBlock.TaskList })
        assertTrue(doc.blocks.any { it is MarkdownBlock.Divider })
    }

    @Test
    fun convertsLegacyHtmlPostsIntoBlocks() {
        val html = "<h2>标题</h2><p>你好 <strong>世界</strong></p><blockquote>引用文本</blockquote><img src=\"https://img.example/a.png\" alt=\"图\" />"
        val doc = MarkdownParser.parse(html, format = "html")
        assertTrue(doc.blocks.any { it is MarkdownBlock.Heading && it.plainText == "标题" })
        assertTrue(doc.blocks.any { it is MarkdownBlock.Blockquote })
        assertEquals(listOf("https://img.example/a.png"), doc.imageUrls)
    }

    @Test
    fun wysiwygRoundTripPreservesStructureAndSupportsUndoRedo() {
        val original = """
            # 文章标题

            第一段正文

            - 条目一
            - 条目二

            ```python
            print("hi")
            ```
        """.trimIndent()

        val blocks = WysiwygCodec.fromMarkdown(original)
        assertEquals(4, blocks.size)
        assertEquals(WysiwygBlockType.HEADING_1, blocks[0].type)
        assertEquals(WysiwygBlockType.CODE_BLOCK, blocks[3].type)

        val serialized = WysiwygCodec.toMarkdown(blocks)
        assertTrue(serialized.contains("# 文章标题"))
        assertTrue(serialized.contains("- 条目一"))
        assertTrue(serialized.contains("```python"))

        var history = WysiwygHistory(present = blocks)
        assertFalse(history.canUndo)
        history = history.commit(blocks + WysiwygBlock(type = WysiwygBlockType.DIVIDER))
        assertTrue(history.canUndo)
        assertEquals(5, history.present.size)
        history = history.undo()
        assertEquals(4, history.present.size)
        assertTrue(history.canRedo)
        history = history.redo()
        assertEquals(5, history.present.size)
    }
}
