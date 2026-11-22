package net.igng.bbs.feature.creator

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Publish
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.data.CreateCreatorPostRequest
import net.igng.bbs.core.data.LocalEditorDraft
import net.igng.bbs.core.data.SubmitCreatorPostRequest
import net.igng.bbs.core.data.UpdateCreatorPostRequest
import net.igng.bbs.core.designsystem.IgngInfoBanner
import net.igng.bbs.core.designsystem.IgngLoadingCenter
import net.igng.bbs.core.designsystem.IgngPrimaryButton
import net.igng.bbs.core.designsystem.IgngRadius
import net.igng.bbs.core.designsystem.IgngSecondaryButton
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTile
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.designsystem.IgngTopBar
import net.igng.bbs.core.markdown.BbsMarkdownDocumentView
import net.igng.bbs.core.markdown.MarkdownParser
import net.igng.bbs.core.markdown.WysiwygBlock
import net.igng.bbs.core.markdown.WysiwygBlockType
import net.igng.bbs.core.markdown.WysiwygCodec
import net.igng.bbs.core.markdown.WysiwygHistory
import net.igng.bbs.core.markdown.WysiwygTaskItem
import net.igng.bbs.core.model.AppPreferences
import net.igng.bbs.core.model.ColumnSummaryDto
import net.igng.bbs.core.network.ApiResult

private enum class EditorMode(val label: String) {
    WYSIWYG("块级编辑"),
    SOURCE("源码"),
    PREVIEW("预览"),
}

/**
 * Custom native Jetpack Compose block-based WYSIWYG article editor for IGNG BBS.
 * Supports 14 block types, inline formatting toolbar, Photo Picker cloud media upload,
 * Undo/Redo history, lossless 1-tap toggle to Markdown/HTML source & live reader preview,
 * local auto-draft recovery, and full publish metadata sheet.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NativeWysiwygEditorScreen(
    container: BbsAppContainer,
    postId: String? = null,
    initialColumnId: String? = null,
    onBack: () -> Unit,
    onPublished: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = IgngTheme.colors
    val bootstrap by container.bootstrap.collectAsState()
    val prefs by container.settingsRepository.preferences.collectAsState(initial = AppPreferences())

    val draftKey = postId ?: "new_post_${initialColumnId ?: "root"}"

    var currentPostId by remember(postId) { mutableStateOf(postId) }
    var title by remember { mutableStateOf("") }
    var format by remember { mutableStateOf("markdown") }
    var history by remember { mutableStateOf(WysiwygHistory()) }
    var rawSource by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(EditorMode.WYSIWYG) }
    var activeBlockId by remember { mutableStateOf<String?>(null) }

    // Metadata for publish sheet
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var selectedColumnId by remember { mutableStateOf(initialColumnId) }
    var visibility by remember { mutableStateOf("public") }
    var summary by remember { mutableStateOf("") }
    var tagsInput by remember { mutableStateOf("") }
    var requestSummary by remember { mutableStateOf(false) }
    var requestTags by remember { mutableStateOf(false) }
    var myColumns by remember { mutableStateOf<List<ColumnSummaryDto>>(emptyList()) }

    var loading by remember { mutableStateOf(postId != null) }
    var busy by remember { mutableStateOf(false) }
    var uploadingImage by remember { mutableStateOf(false) }
    var statusBanner by remember { mutableStateOf<String?>(null) }
    var recoverableDraft by remember { mutableStateOf<LocalEditorDraft?>(null) }
    var showAddBlockSheet by remember { mutableStateOf(false) }
    var convertTargetBlockId by remember { mutableStateOf<String?>(null) }
    var showPublishSheet by remember { mutableStateOf(false) }
    var pendingImageBlockId by remember { mutableStateOf<String?>(null) }

    fun updateBlocks(next: List<WysiwygBlock>) {
        val safe = next.ifEmpty { listOf(WysiwygBlock(type = WysiwygBlockType.PARAGRAPH)) }
        history = history.commit(safe)
        rawSource = WysiwygCodec.toMarkdown(safe)
    }

    fun switchMode(target: EditorMode) {
        if (target == mode) return
        if (mode == EditorMode.SOURCE && target != EditorMode.SOURCE) {
            val parsed = WysiwygCodec.fromMarkdown(rawSource, format)
            history = history.commit(parsed)
        } else if (mode == EditorMode.WYSIWYG && target != EditorMode.WYSIWYG) {
            rawSource = WysiwygCodec.toMarkdown(history.present)
        }
        mode = target
    }

    // Native Android Photo Picker for image block insertion/upload
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploadingImage = true
            statusBanner = null
            val bytesAndMeta = withContext(Dispatchers.IO) {
                runCatching {
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val ext = when {
                        mime.contains("png") -> "png"
                        mime.contains("webp") -> "webp"
                        mime.contains("gif") -> "gif"
                        else -> "jpg"
                    }
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes == null) null else Triple(bytes, mime, "bbs_${System.currentTimeMillis()}.$ext")
                }.getOrNull()
            }
            if (bytesAndMeta == null) {
                statusBanner = "无法读取所选图片，请重新选择"
                uploadingImage = false
                return@launch
            }
            val (bytes, mime, fileName) = bytesAndMeta
            when (val res = container.creatorRepository.uploadMedia(fileName, mime, bytes)) {
                is ApiResult.Success -> {
                    val mdUrl = res.data.markdownUrl.ifBlank { res.data.url }
                    val targetId = pendingImageBlockId
                    val blocks = history.present.toMutableList()
                    val existingIdx = targetId?.let { id -> blocks.indexOfFirst { it.id == id } } ?: -1
                    if (existingIdx >= 0) {
                        blocks[existingIdx] = blocks[existingIdx].copy(imageUrl = mdUrl)
                    } else {
                        val newImg = WysiwygBlock(type = WysiwygBlockType.IMAGE, imageUrl = mdUrl, caption = "")
                        val activeIdx = activeBlockId?.let { id -> blocks.indexOfFirst { it.id == id } } ?: blocks.lastIndex
                        blocks.add((activeIdx + 1).coerceIn(0, blocks.size), newImg)
                        activeBlockId = newImg.id
                    }
                    updateBlocks(blocks)
                    statusBanner = "图片已上传并插入正文"
                }
                is ApiResult.Failure -> statusBanner = res.error.message
            }
            pendingImageBlockId = null
            uploadingImage = false
        }
    }

    // Load initial post & columns & check local auto-draft
    LaunchedEffect(postId) {
        if (selectedCategoryId == null) {
            selectedCategoryId = bootstrap?.categories?.firstOrNull()?.id
        }
        when (val cols = container.creatorRepository.listColumns()) {
            is ApiResult.Success -> myColumns = cols.data.data
            is ApiResult.Failure -> Unit
        }
        if (postId != null) {
            loading = true
            when (val res = container.creatorRepository.getPostDetail(postId)) {
                is ApiResult.Success -> {
                    val d = res.data
                    val srcTitle = d.draft?.title ?: d.title
                    val srcContent = d.draft?.content ?: d.content
                    val srcFormat = d.draft?.format ?: d.format
                    title = srcTitle
                    format = srcFormat
                    summary = d.draft?.summary ?: d.summary.orEmpty()
                    tagsInput = (d.draft?.tags?.takeIf { it.isNotEmpty() } ?: d.tags).joinToString(", ")
                    selectedCategoryId = d.category?.id ?: selectedCategoryId
                    selectedColumnId = d.column?.id ?: selectedColumnId
                    visibility = d.visibility
                    rawSource = srcContent
                    history = WysiwygHistory(present = WysiwygCodec.fromMarkdown(srcContent, srcFormat))
                }
                is ApiResult.Failure -> statusBanner = res.error.message
            }
            loading = false
        }
        val local = container.settingsRepository.loadEditorDraft(draftKey)
        if (local != null && (local.title.isNotBlank() || local.content.isNotBlank())) {
            recoverableDraft = local
        }
    }

    // Local auto-save every 2.5s after edits
    LaunchedEffect(title, rawSource, summary, tagsInput, selectedCategoryId, selectedColumnId, visibility) {
        if (loading) return@LaunchedEffect
        if (title.isBlank() && rawSource.isBlank()) return@LaunchedEffect
        delay(2500L)
        val finalContent = if (mode == EditorMode.WYSIWYG) WysiwygCodec.toMarkdown(history.present) else rawSource
        container.settingsRepository.saveEditorDraft(
            key = draftKey,
            draft = LocalEditorDraft(
                postId = currentPostId,
                title = title,
                content = finalContent,
                summary = summary,
                tags = parseTagsCsv(tagsInput),
                categoryId = selectedCategoryId,
                columnId = selectedColumnId,
                visibility = visibility,
                format = format,
            ),
        )
    }

    fun saveOrPublish(saveAsDraft: Boolean) {
        val cleanTitle = title.trim()
        val finalContent = if (mode == EditorMode.WYSIWYG) WysiwygCodec.toMarkdown(history.present) else rawSource
        if (cleanTitle.isEmpty()) {
            statusBanner = "请先填写文章标题"
            return
        }
        if (finalContent.trim().isEmpty()) {
            statusBanner = "正文内容不能为空"
            return
        }
        val catId = selectedCategoryId ?: bootstrap?.categories?.firstOrNull()?.id
        if (catId == null) {
            statusBanner = "请先选择一个文章分类"
            return
        }
        val tags = parseTagsCsv(tagsInput)
        val cleanSummary = summary.trim().ifBlank { null }

        scope.launch {
            busy = true
            statusBanner = null
            if (currentPostId == null) {
                when (val res = container.creatorRepository.createPost(
                    CreateCreatorPostRequest(
                        categoryId = catId,
                        title = cleanTitle,
                        content = finalContent,
                        summary = cleanSummary,
                        tags = tags,
                        format = format,
                        visibility = visibility,
                        columnId = selectedColumnId,
                        saveAsDraft = saveAsDraft,
                        requestSummary = requestSummary,
                        requestTags = requestTags,
                    ),
                )) {
                    is ApiResult.Success -> {
                        currentPostId = res.data.id
                        container.settingsRepository.clearEditorDraft(draftKey)
                        showPublishSheet = false
                        if (saveAsDraft) {
                            statusBanner = "草稿已保存到云端"
                        } else {
                            onPublished(res.data.id)
                        }
                    }
                    is ApiResult.Failure -> statusBanner = res.error.message
                }
            } else {
                val pid = currentPostId!!
                val upd = container.creatorRepository.updatePost(
                    pid,
                    UpdateCreatorPostRequest(
                        title = cleanTitle,
                        content = finalContent,
                        summary = cleanSummary,
                        tags = tags,
                        format = format,
                        categoryId = catId,
                        columnId = selectedColumnId,
                        visibility = visibility,
                    ),
                )
                if (upd is ApiResult.Failure) {
                    statusBanner = upd.error.message
                    busy = false
                    return@launch
                }
                if (saveAsDraft) {
                    container.settingsRepository.clearEditorDraft(draftKey)
                    showPublishSheet = false
                    statusBanner = "更改已保存为草稿"
                } else {
                    when (val sub = container.creatorRepository.submitPost(
                        pid,
                        SubmitCreatorPostRequest(
                            title = cleanTitle,
                            content = finalContent,
                            summary = cleanSummary,
                            tags = tags,
                            format = format,
                            requestSummary = requestSummary,
                            requestTags = requestTags,
                        ),
                    )) {
                        is ApiResult.Success -> {
                            container.settingsRepository.clearEditorDraft(draftKey)
                            showPublishSheet = false
                            onPublished(sub.data.postId)
                        }
                        is ApiResult.Failure -> statusBanner = sub.error.message
                    }
                }
            }
            busy = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding()
            .imePadding(),
    ) {
        IgngTopBar(
            title = if (currentPostId == null) "新建文章" else "编辑文章",
            onBack = onBack,
            actions = {
                if (mode == EditorMode.WYSIWYG) {
                    IconButton(onClick = { history = history.undo(); rawSource = WysiwygCodec.toMarkdown(history.present) }, enabled = history.canUndo) {
                        Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "撤销", tint = if (history.canUndo) colors.textPrimary else colors.textMuted)
                    }
                    IconButton(onClick = { history = history.redo(); rawSource = WysiwygCodec.toMarkdown(history.present) }, enabled = history.canRedo) {
                        Icon(Icons.AutoMirrored.Rounded.Redo, contentDescription = "重做", tint = if (history.canRedo) colors.textPrimary else colors.textMuted)
                    }
                }
                IconButton(onClick = { saveOrPublish(saveAsDraft = true) }, enabled = !busy) {
                    Icon(Icons.Rounded.Save, contentDescription = "保存草稿", tint = colors.textPrimary)
                }
                IgngTagChip(
                    text = if (busy) "提交中…" else "发布",
                    tone = IgngTone.ACCENT,
                    selected = true,
                    onClick = { if (!busy) showPublishSheet = true },
                )
                Spacer(Modifier.width(4.dp))
            },
        )

        // Mode switcher + quick actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EditorMode.entries.forEach { m ->
                IgngTagChip(text = m.label, selected = mode == m, onClick = { switchMode(m) })
            }
            Spacer(Modifier.weight(1f))
            if (uploadingImage) {
                IgngTagChip(text = "上传图片中…", tone = IgngTone.WARNING)
            }
        }

        if (loading) {
            IgngLoadingCenter(loading = true)
            return
        }

        recoverableDraft?.let { saved ->
            Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                IgngInfoBanner(
                    title = "发现未提交的本地草稿",
                    message = "标题：${saved.title.ifBlank { "（未命名）" }}",
                    tone = IgngTone.WARNING,
                    actionLabel = "恢复草稿",
                    onAction = {
                        title = saved.title
                        rawSource = saved.content
                        summary = saved.summary
                        tagsInput = saved.tags.joinToString(", ")
                        format = saved.format
                        history = WysiwygHistory(present = WysiwygCodec.fromMarkdown(saved.content, saved.format))
                        recoverableDraft = null
                    },
                )
            }
        }

        statusBanner?.let { msg ->
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                IgngInfoBanner(title = "编辑提示", message = msg, tone = IgngTone.ACCENT)
            }
        }

        // Main Editor Body
        when (mode) {
            EditorMode.WYSIWYG -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Title field
                    item(key = "editor_title") {
                        IgngTile(padding = PaddingValues(16.dp)) {
                            BasicTextField(
                                value = title,
                                onValueChange = { title = it },
                                textStyle = MaterialTheme.typography.headlineSmall.copy(
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Bold,
                                ),
                                cursorBrush = SolidColor(colors.accent),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (title.isEmpty()) {
                                        Text("输入吸引读者的文章标题…", style = MaterialTheme.typography.headlineSmall, color = colors.textMuted)
                                    }
                                    inner()
                                },
                            )
                        }
                    }

                    // WYSIWYG Blocks Canvas
                    itemsIndexed(history.present, key = { _, b -> b.id }) { index, block ->
                        val isActive = activeBlockId == block.id
                        WysiwygBlockEditorCard(
                            block = block,
                            isActive = isActive,
                            canMoveUp = index > 0,
                            canMoveDown = index < history.present.lastIndex,
                            resolveImageUrl = { raw -> container.endpoints.resolveMediaUrl(raw, currentPostId) ?: raw },
                            onActivate = { activeBlockId = block.id },
                            onChange = { updated ->
                                val next = history.present.toMutableList()
                                next[index] = updated
                                updateBlocks(next)
                            },
                            onMoveUp = {
                                if (index > 0) {
                                    val next = history.present.toMutableList()
                                    val item = next.removeAt(index)
                                    next.add(index - 1, item)
                                    updateBlocks(next)
                                }
                            },
                            onMoveDown = {
                                if (index < history.present.lastIndex) {
                                    val next = history.present.toMutableList()
                                    val item = next.removeAt(index)
                                    next.add(index + 1, item)
                                    updateBlocks(next)
                                }
                            },
                            onConvertRequest = { convertTargetBlockId = block.id },
                            onDelete = {
                                val next = history.present.toMutableList()
                                next.removeAt(index)
                                updateBlocks(next)
                            },
                            onPickImage = {
                                pendingImageBlockId = block.id
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        )
                    }

                    // Add Block Bar
                    item(key = "add_block_btn") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            IgngSecondaryButton(
                                text = "添加内容块",
                                icon = Icons.Rounded.Add,
                                onClick = { showAddBlockSheet = true },
                                modifier = Modifier.weight(1f),
                            )
                            IgngSecondaryButton(
                                text = "插入图片",
                                icon = Icons.Rounded.Image,
                                onClick = {
                                    pendingImageBlockId = null
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // Bottom quick inline formatting ribbon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.tile)
                        .border(1.dp, colors.line)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    fun wrapActive(prefix: String, suffix: String = prefix, placeholder: String = "文字") {
                        val idx = activeBlockId?.let { id -> history.present.indexOfFirst { it.id == id } }
                            ?.takeIf { it >= 0 } ?: history.present.lastIndex
                        val target = history.present[idx]
                        val updated = target.copy(text = WysiwygCodec.applyInlineWrap(target.text, prefix, suffix, placeholder))
                        val next = history.present.toMutableList()
                        next[idx] = updated
                        updateBlocks(next)
                    }
                    IgngTagChip(text = "+ 块", tone = IgngTone.ACCENT, onClick = { showAddBlockSheet = true })
                    IgngTagChip(text = "加粗", onClick = { wrapActive("**", "**", "加粗文字") })
                    IgngTagChip(text = "斜体", onClick = { wrapActive("*", "*", "斜体文字") })
                    IgngTagChip(text = "行内代码", onClick = { wrapActive("`", "`", "code") })
                    IgngTagChip(text = "高亮", onClick = { wrapActive("==", "==", "重点") })
                    IgngTagChip(text = "删除线", onClick = { wrapActive("~~", "~~", "文本") })
                    IgngTagChip(text = "链接", onClick = { wrapActive("[", "](https://)", "链接文字") })
                }
            }

            EditorMode.SOURCE -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("文章标题") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = rawSource,
                        onValueChange = { rawSource = it },
                        label = { Text("Markdown / HTML 源码") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.5.sp),
                    )
                }
            }

            EditorMode.PREVIEW -> {
                val doc = remember(rawSource, format) {
                    MarkdownParser.parse(
                        source = rawSource,
                        format = format,
                        resolveUrl = { raw -> container.endpoints.resolveMediaUrl(raw, currentPostId) ?: raw },
                    )
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        Text(
                            text = title.ifBlank { "（未填写标题）" },
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = colors.line)
                    }
                    item {
                        BbsMarkdownDocumentView(
                            document = doc,
                            typography = prefs.reader,
                            onImageClick = {},
                            onLinkClick = {},
                        )
                    }
                }
            }
        }
    }

    // Add Block Palette Bottom Sheet
    if (showAddBlockSheet) {
        ModalBottomSheet(onDismissRequest = { showAddBlockSheet = false }, containerColor = colors.tile) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("插入内容块", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WysiwygBlockType.entries.forEach { type ->
                        IgngTagChip(
                            text = type.label,
                            tone = if (type == WysiwygBlockType.IMAGE) IgngTone.ACCENT else IgngTone.NEUTRAL,
                            onClick = {
                                showAddBlockSheet = false
                                if (type == WysiwygBlockType.IMAGE) {
                                    pendingImageBlockId = null
                                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                } else {
                                    val newBlock = WysiwygBlock.emptyOf(type)
                                    val next = history.present.toMutableList()
                                    val idx = activeBlockId?.let { id -> next.indexOfFirst { it.id == id } } ?: next.lastIndex
                                    next.add((idx + 1).coerceIn(0, next.size), newBlock)
                                    activeBlockId = newBlock.id
                                    updateBlocks(next)
                                }
                            },
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // Convert Block Type Sheet
    convertTargetBlockId?.let { targetId ->
        ModalBottomSheet(onDismissRequest = { convertTargetBlockId = null }, containerColor = colors.tile) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("转换当前块类型", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WysiwygBlockType.entries.filterNot { it == WysiwygBlockType.IMAGE || it == WysiwygBlockType.VIDEO }.forEach { t ->
                        IgngTagChip(
                            text = t.label,
                            onClick = {
                                val next = history.present.map { b ->
                                    if (b.id == targetId) WysiwygCodec.convertBlockType(b, t) else b
                                }
                                updateBlocks(next)
                                convertTargetBlockId = null
                            },
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // Publish Settings Sheet
    if (showPublishSheet) {
        ModalBottomSheet(onDismissRequest = { showPublishSheet = false }, containerColor = colors.tile) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("发布与文章设置", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)

                Text("所属分区", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    bootstrap?.categories.orEmpty().forEach { cat ->
                        IgngTagChip(
                            text = cat.name,
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                        )
                    }
                }

                if (myColumns.isNotEmpty()) {
                    Text("投稿到专栏（可选）", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        IgngTagChip(text = "不投专栏", selected = selectedColumnId == null, onClick = { selectedColumnId = null })
                        myColumns.forEach { col ->
                            IgngTagChip(
                                text = col.name,
                                selected = selectedColumnId == col.id,
                                onClick = { selectedColumnId = col.id },
                            )
                        }
                    }
                }

                Text("可见范围", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("public" to "公开", "followers" to "仅粉丝", "following" to "仅关注", "private" to "私密").forEach { (k, label) ->
                        IgngTagChip(text = label, selected = visibility == k, onClick = { visibility = k })
                    }
                }

                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = { tagsInput = it },
                    label = { Text("标签（逗号分隔，最多 8 个）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("文章摘要（留空可由 AI 生成）") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("发布时请求 AI 生成摘要", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Switch(checked = requestSummary, onCheckedChange = { requestSummary = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("发布时请求 AI 推荐标签", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Switch(checked = requestTags, onCheckedChange = { requestTags = it })
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    IgngSecondaryButton(
                        text = "存为草稿",
                        icon = Icons.Rounded.Save,
                        enabled = !busy,
                        onClick = { saveOrPublish(saveAsDraft = true) },
                        modifier = Modifier.weight(1f),
                    )
                    IgngPrimaryButton(
                        text = if (busy) "提交中…" else "提交发布",
                        icon = Icons.Rounded.Publish,
                        enabled = !busy,
                        onClick = { saveOrPublish(saveAsDraft = false) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(22.dp))
            }
        }
    }
}

@Composable
private fun WysiwygBlockEditorCard(
    block: WysiwygBlock,
    isActive: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    resolveImageUrl: (String) -> String,
    onActivate: () -> Unit,
    onChange: (WysiwygBlock) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onConvertRequest: () -> Unit,
    onDelete: () -> Unit,
    onPickImage: () -> Unit,
) {
    val colors = IgngTheme.colors
    val borderColor = if (isActive) colors.accent else colors.line
    val shape = RoundedCornerShape(IgngRadius.card)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.tile)
            .border(if (isActive) 1.5.dp else 1.dp, borderColor, shape)
            .clickable(onClick = onActivate)
            .padding(14.dp),
    ) {
        // Active Block Action Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = block.type.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) colors.accent else colors.textMuted,
            )
            AnimatedVisibility(visible = isActive) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.ArrowUpward, contentDescription = "上移", tint = if (canMoveUp) colors.textSecondary else colors.lineStrong, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.ArrowDownward, contentDescription = "下移", tint = if (canMoveDown) colors.textSecondary else colors.lineStrong, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onConvertRequest, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.SwapHoriz, contentDescription = "转换类型", tint = colors.accent, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "删除块", tint = colors.danger, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))

        when (block.type) {
            WysiwygBlockType.HEADING_1,
            WysiwygBlockType.HEADING_2,
            WysiwygBlockType.HEADING_3,
            WysiwygBlockType.HEADING_4,
            WysiwygBlockType.PARAGRAPH,
            -> {
                val style = when (block.type) {
                    WysiwygBlockType.HEADING_1 -> MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    WysiwygBlockType.HEADING_2 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    WysiwygBlockType.HEADING_3 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    WysiwygBlockType.HEADING_4 -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    else -> MaterialTheme.typography.bodyLarge.copy(color = colors.textPrimary)
                }
                BasicTextField(
                    value = block.text,
                    onValueChange = { onActivate(); onChange(block.copy(text = it)) },
                    textStyle = style,
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (block.text.isEmpty()) Text("在此输入${block.type.label}…", style = style.copy(color = colors.textMuted))
                        inner()
                    },
                )
            }

            WysiwygBlockType.BLOCKQUOTE -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(IgngRadius.control))
                        .background(colors.tileSoft)
                        .padding(10.dp),
                ) {
                    Box(modifier = Modifier.width(3.dp).height(40.dp).background(colors.accent))
                    Spacer(Modifier.width(10.dp))
                    BasicTextField(
                        value = block.text,
                        onValueChange = { onActivate(); onChange(block.copy(text = it)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (block.text.isEmpty()) Text("输入引用内容…", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                            inner()
                        },
                    )
                }
            }

            WysiwygBlockType.CODE_BLOCK -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        listOf("kotlin", "java", "javascript", "python", "bash", "json", "sql").forEach { lang ->
                            IgngTagChip(
                                text = lang,
                                selected = block.language.equals(lang, ignoreCase = true),
                                onClick = { onActivate(); onChange(block.copy(language = lang)) },
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(IgngRadius.control))
                            .background(colors.codeBackground)
                            .padding(12.dp),
                    ) {
                        BasicTextField(
                            value = block.text,
                            onValueChange = { onActivate(); onChange(block.copy(text = it)) },
                            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = colors.textPrimary),
                            cursorBrush = SolidColor(colors.accent),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { inner ->
                                if (block.text.isEmpty()) Text("// 输入代码片段…", style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = colors.textMuted))
                                inner()
                            },
                        )
                    }
                }
            }

            WysiwygBlockType.BULLET_LIST, WysiwygBlockType.ORDERED_LIST -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    block.listItems.forEachIndexed { idx, itemText ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (block.type == WysiwygBlockType.ORDERED_LIST) "${idx + 1}." else "•",
                                style = MaterialTheme.typography.titleSmall,
                                color = colors.accent,
                            )
                            BasicTextField(
                                value = itemText,
                                onValueChange = { v ->
                                    onActivate()
                                    val next = block.listItems.toMutableList()
                                    next[idx] = v
                                    onChange(block.copy(listItems = next))
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                                cursorBrush = SolidColor(colors.accent),
                                modifier = Modifier.weight(1f),
                                decorationBox = { inner ->
                                    if (itemText.isEmpty()) Text("列表条目…", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                                    inner()
                                },
                            )
                            if (block.listItems.size > 1) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "删除条目",
                                    tint = colors.textMuted,
                                    modifier = Modifier.size(16.dp).clickable {
                                        val next = block.listItems.toMutableList().apply { removeAt(idx) }
                                        onChange(block.copy(listItems = next))
                                    },
                                )
                            }
                        }
                    }
                    IgngTagChip(text = "+ 添加一项", onClick = { onChange(block.copy(listItems = block.listItems + "")) })
                }
            }

            WysiwygBlockType.TASK_LIST -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    block.taskItems.forEachIndexed { idx, item ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = if (item.checked) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                                contentDescription = "切换勾选",
                                tint = if (item.checked) colors.success else colors.textMuted,
                                modifier = Modifier.size(20.dp).clickable {
                                    val next = block.taskItems.toMutableList()
                                    next[idx] = item.copy(checked = !item.checked)
                                    onChange(block.copy(taskItems = next))
                                },
                            )
                            BasicTextField(
                                value = item.text,
                                onValueChange = { v ->
                                    val next = block.taskItems.toMutableList()
                                    next[idx] = item.copy(text = v)
                                    onChange(block.copy(taskItems = next))
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                                cursorBrush = SolidColor(colors.accent),
                                modifier = Modifier.weight(1f),
                                decorationBox = { inner ->
                                    if (item.text.isEmpty()) Text("待办事项…", style = MaterialTheme.typography.bodyMedium, color = colors.textMuted)
                                    inner()
                                },
                            )
                        }
                    }
                    IgngTagChip(text = "+ 添加待办", onClick = { onChange(block.copy(taskItems = block.taskItems + WysiwygTaskItem())) })
                }
            }

            WysiwygBlockType.TABLE -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        block.tableHeaders.forEachIndexed { cIdx, header ->
                            OutlinedTextField(
                                value = header,
                                onValueChange = { v ->
                                    val nextH = block.tableHeaders.toMutableList().apply { this[cIdx] = v }
                                    onChange(block.copy(tableHeaders = nextH))
                                },
                                singleLine = true,
                                label = { Text("表头 ${cIdx + 1}") },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    block.tableRows.forEachIndexed { rIdx, row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            block.tableHeaders.indices.forEach { cIdx ->
                                OutlinedTextField(
                                    value = row.getOrElse(cIdx) { "" },
                                    onValueChange = { v ->
                                        val nextRows = block.tableRows.mapIndexed { ri, r ->
                                            if (ri != rIdx) r else List(block.tableHeaders.size) { ci -> if (ci == cIdx) v else r.getOrElse(ci) { "" } }
                                        }
                                        onChange(block.copy(tableRows = nextRows))
                                    },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IgngTagChip(text = "+ 加一行", onClick = {
                            onChange(block.copy(tableRows = block.tableRows + listOf(List(block.tableHeaders.size) { "" })))
                        })
                        if (block.tableHeaders.size < 4) {
                            IgngTagChip(text = "+ 加一列", onClick = {
                                onChange(
                                    block.copy(
                                        tableHeaders = block.tableHeaders + "列 ${block.tableHeaders.size + 1}",
                                        tableRows = block.tableRows.map { it + "" },
                                    ),
                                )
                            })
                        }
                    }
                }
            }

            WysiwygBlockType.DIVIDER -> {
                HorizontalDivider(color = colors.lineStrong, modifier = Modifier.padding(vertical = 8.dp))
            }

            WysiwygBlockType.IMAGE -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (block.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = resolveImageUrl(block.imageUrl),
                            contentDescription = block.caption,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(IgngRadius.control))
                                .background(colors.tileSoft),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IgngTagChip(text = if (block.imageUrl.isBlank()) "从相册选择并上传" else "更换图片", tone = IgngTone.ACCENT, onClick = onPickImage)
                    }
                    OutlinedTextField(
                        value = block.caption,
                        onValueChange = { onChange(block.copy(caption = it)) },
                        label = { Text("图注说明（选填）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            WysiwygBlockType.VIDEO -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = block.imageUrl,
                        onValueChange = { onChange(block.copy(imageUrl = it)) },
                        label = { Text("视频地址（https://...）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = block.caption,
                        onValueChange = { onChange(block.copy(caption = it)) },
                        label = { Text("视频标题") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private fun parseTagsCsv(raw: String): List<String> =
    raw.split(',', '，', '、')
        .map { it.trim().removePrefix("#") }
        .filter { it.isNotEmpty() }
        .distinct()
        .take(8)
