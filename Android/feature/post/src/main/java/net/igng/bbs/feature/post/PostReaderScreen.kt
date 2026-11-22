package net.igng.bbs.feature.post

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.data.CreateReportRequest
import net.igng.bbs.core.designsystem.BbsFloatingArticleBar
import net.igng.bbs.core.designsystem.IgngAvatar
import net.igng.bbs.core.designsystem.IgngErrorTile
import net.igng.bbs.core.designsystem.IgngImageGalleryLightbox
import net.igng.bbs.core.designsystem.IgngInfoBanner
import net.igng.bbs.core.designsystem.IgngLoadingCenter
import net.igng.bbs.core.designsystem.IgngPrimaryButton
import net.igng.bbs.core.designsystem.IgngRadius
import net.igng.bbs.core.designsystem.IgngSecondaryButton
import net.igng.bbs.core.designsystem.IgngSectionHeader
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTile
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.designsystem.IgngTopBar
import net.igng.bbs.core.designsystem.readerSurfaceColors
import net.igng.bbs.core.markdown.BbsMarkdownBlockView
import net.igng.bbs.core.markdown.MarkdownParser
import net.igng.bbs.core.model.AppPreferences
import net.igng.bbs.core.model.BbsFormatters
import net.igng.bbs.core.model.LocalReadingHistoryEntry
import net.igng.bbs.core.model.PostDetailDto
import net.igng.bbs.core.model.ReaderCanvasTone
import net.igng.bbs.core.network.ApiResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PostReaderScreen(
    container: BbsAppContainer,
    postId: String,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenColumn: (String) -> Unit,
    onOpenUser: (String) -> Unit,
    onOpenTag: (String) -> Unit,
    onOpenLink: (String) -> Unit,
    onEditPost: (String) -> Unit,
    onRequireLogin: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val colors = IgngTheme.colors
    val prefs by container.settingsRepository.preferences.collectAsState(initial = AppPreferences())
    val bootstrap by container.bootstrap.collectAsState()
    val activeAccount by container.sessionStore.activeAccount.collectAsState()

    val (readerBg, readerPrimary, readerSecondary) = colors.readerSurfaceColors(prefs.reader.canvasTone)

    var detail by remember { mutableStateOf<PostDetailDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var acknowledgedRestriction by remember(postId) { mutableStateOf(false) }

    var liked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableIntStateOf(0) }
    var favorited by remember { mutableStateOf(false) }
    var followingAuthor by remember { mutableStateOf(false) }

    var showTocSheet by remember { mutableStateOf(false) }
    var showTypoSheet by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    var lightboxIndex by remember { mutableStateOf<Int?>(null) }
    var feedbackBanner by remember { mutableStateOf<String?>(null) }

    suspend fun loadPost() {
        loading = true
        error = null
        when (val res = container.postRepository.getPostDetail(postId)) {
            is ApiResult.Success -> {
                detail = res.data
                liked = res.data.viewerState.isLiked
                likeCount = res.data.likeCount
                favorited = res.data.viewerState.isFavorited
                followingAuthor = res.data.viewerState.isFollowingAuthor
            }
            is ApiResult.Failure -> error = res.error.message
        }
        loading = false
    }

    LaunchedEffect(postId, activeAccount?.userId) {
        loadPost()
    }

    // Record view after brief dwell
    LaunchedEffect(detail?.id) {
        val current = detail ?: return@LaunchedEffect
        delay(1200L)
        container.postRepository.recordView(current.id)
    }

    val document = remember(detail?.content, detail?.format, detail?.id) {
        val d = detail ?: return@remember null
        MarkdownParser.parse(
            source = d.content,
            format = d.format,
            resolveUrl = { raw ->
                d.mediaUrls[raw] ?: container.endpoints.resolveMediaUrl(raw, d.id) ?: raw
            },
        )
    }

    val listState = rememberLazyListState()
    val headerItemsCount = 2 // meta header + optional summary/column
    val totalBlocks = document?.blocks?.size ?: 0
    val commentsAnchorIndex = headerItemsCount + totalBlocks + 1

    val readingProgress by remember(totalBlocks) {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount.coerceAtLeast(1)
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            (lastVisible.toFloat() / totalItems.toFloat()).coerceIn(0f, 1f)
        }
    }

    var lastIndex by remember { mutableIntStateOf(0) }
    var barVisible by remember { mutableStateOf(true) }
    LaunchedEffect(listState.firstVisibleItemIndex) {
        val cur = listState.firstVisibleItemIndex
        barVisible = cur <= lastIndex || cur <= 1
        lastIndex = cur
    }

    // Save local reading history periodically
    LaunchedEffect(detail?.id, listState.firstVisibleItemIndex) {
        val d = detail ?: return@LaunchedEffect
        container.settingsRepository.recordReadingProgress(
            LocalReadingHistoryEntry(
                postId = d.id,
                numericId = d.numericId,
                title = d.title,
                authorName = d.author?.displayName ?: "匿名用户",
                categoryName = d.category?.name,
                progressRatio = readingProgress,
                scrollIndex = listState.firstVisibleItemIndex,
            ),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(readerBg)
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            IgngTopBar(
                title = detail?.category?.name ?: "文章阅读",
                onBack = onBack,
                actions = {
                    if (document?.toc?.isNotEmpty() == true) {
                        IconButton(onClick = { showTocSheet = true }) {
                            Icon(Icons.AutoMirrored.Rounded.FormatListBulleted, contentDescription = "文章目录", tint = readerPrimary)
                        }
                    }
                    IconButton(onClick = { showTypoSheet = true }) {
                        Icon(Icons.Rounded.TextFields, contentDescription = "阅读排版", tint = readerPrimary)
                    }
                    if (detail?.viewerState?.canEdit == true) {
                        IconButton(onClick = { onEditPost(detail!!.id) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "编辑文章", tint = colors.accent)
                        }
                    }
                    if (detail != null) {
                        IconButton(onClick = {
                            if (activeAccount == null) {
                                onRequireLogin()
                            } else {
                                reportTarget = "post" to detail!!.id
                                showReportDialog = true
                            }
                        }) {
                            Icon(Icons.Rounded.Flag, contentDescription = "举报", tint = readerSecondary)
                        }
                    }
                },
            )

            // Hairline reading progress indicator
            LinearProgressIndicator(
                progress = { readingProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = colors.accent,
                trackColor = colors.line,
            )

            when {
                loading && detail == null -> IgngLoadingCenter(loading = true)
                error != null && detail == null -> Box(Modifier.padding(16.dp)) {
                    IgngErrorTile(message = error!!, onRetry = { scope.launch { loadPost() } })
                }
                detail != null -> {
                    val d = detail!!
                    // Restricted warning gate (ban_status = 4)
                    if (d.restrictedWarning && !acknowledgedRestriction) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            IgngTile {
                                Text("内容受限提示", style = MaterialTheme.typography.titleLarge, color = colors.warning)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = d.restrictedReason?.ifBlank { null }
                                        ?: "本文被标记为受限制内容，仅限通过链接访问并确认后浏览。",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary,
                                )
                                Spacer(Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    IgngSecondaryButton(text = "返回", onClick = onBack, modifier = Modifier.weight(1f))
                                    IgngPrimaryButton(
                                        text = "确认继续阅读",
                                        onClick = { acknowledgedRestriction = true },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 104.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            // Item 0: Title, Author, Collaborators, Meta
                            item(key = "post_header") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    feedbackBanner?.let { msg ->
                                        IgngInfoBanner(title = "提示", message = msg, tone = IgngTone.ACCENT)
                                    }
                                    if (d.moderationStatus == "pending" || d.moderationStatus == "rejected") {
                                        IgngInfoBanner(
                                            title = if (d.moderationStatus == "pending") "审核处理中" else "审核未通过",
                                            message = d.rejectionReason?.ifBlank { null }
                                                ?: "当前版本状态为：${BbsFormatters.postStatusLabel(d.status, d.banStatus, d.moderationStatus)}",
                                            tone = if (d.moderationStatus == "rejected") IgngTone.DANGER else IgngTone.WARNING,
                                        )
                                    }
                                    Text(
                                        text = d.title,
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = readerPrimary,
                                    )
                                    // Author row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        IgngAvatar(
                                            avatarUrl = d.author?.avatarUrl,
                                            displayName = d.author?.displayName ?: "作者",
                                            size = 38.dp,
                                            onClick = d.author?.id?.let { uid -> { onOpenUser(uid) } },
                                        )
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { d.author?.id?.let(onOpenUser) },
                                        ) {
                                            Text(
                                                text = d.author?.displayName ?: "匿名用户",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = readerPrimary,
                                            )
                                            Text(
                                                text = "${BbsFormatters.relativeTime(d.createdAt)} · ${BbsFormatters.readingMinutesLabel(d.readingMinutes)} · ${BbsFormatters.compactCount(d.views)} 阅读",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = readerSecondary,
                                            )
                                        }
                                        if (!d.viewerState.isOwner && d.author != null) {
                                            IgngTagChip(
                                                text = if (followingAuthor) "已关注" else "+ 关注",
                                                selected = followingAuthor,
                                                tone = IgngTone.ACCENT,
                                                onClick = {
                                                    if (activeAccount == null) { onRequireLogin(); return@IgngTagChip }
                                                    scope.launch {
                                                        when (val r = container.profileRepository.toggleFollow(d.author!!.id)) {
                                                            is ApiResult.Success -> followingAuthor = r.data.following
                                                            is ApiResult.Failure -> feedbackBanner = r.error.message
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    }
                                    // Collaborators bylines
                                    if (d.collaborators.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            d.collaborators.forEach { c ->
                                                IgngTagChip(
                                                    text = "${c.roleTitle}：${c.user?.displayName ?: "协作者"}",
                                                    tone = IgngTone.NEUTRAL,
                                                    onClick = c.user?.id?.let { uid -> { onOpenUser(uid) } },
                                                )
                                            }
                                        }
                                    }
                                    // Tags
                                    if (d.tags.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            d.tags.forEach { tag ->
                                                IgngTagChip(text = "#$tag", onClick = { onOpenTag(tag) })
                                            }
                                        }
                                    }
                                }
                            }

                            // Item 1: Summary + Column card
                            item(key = "post_context_cards") {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    d.summary?.takeIf { it.isNotBlank() }?.let { summary ->
                                        IgngTile(soft = true, padding = PaddingValues(14.dp)) {
                                            Text("摘要", style = MaterialTheme.typography.labelLarge, color = colors.accent)
                                            Spacer(Modifier.height(4.dp))
                                            Text(summary, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                                        }
                                    }
                                    d.column?.let { col ->
                                        IgngTile(
                                            soft = true,
                                            padding = PaddingValues(14.dp),
                                            onClick = { onOpenColumn(col.id) },
                                        ) {
                                            Text("收录于专栏 · ${col.name}", style = MaterialTheme.typography.titleSmall, color = colors.accent)
                                            col.description?.takeIf { it.isNotBlank() }?.let {
                                                Spacer(Modifier.height(2.dp))
                                                Text(it, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 1)
                                            }
                                        }
                                    }
                                    HorizontalDivider(color = colors.line)
                                }
                            }

                            // Native Markdown / HTML AST Blocks
                            val blocks = document?.blocks.orEmpty()
                            itemsIndexed(blocks, key = { idx, _ -> "blk_$idx" }) { _, block ->
                                BbsMarkdownBlockView(
                                    block = block,
                                    typography = prefs.reader,
                                    onImageClick = { url ->
                                        val idx = document?.imageUrls?.indexOf(url) ?: -1
                                        if (idx >= 0) lightboxIndex = idx
                                    },
                                    onLinkClick = onOpenLink,
                                    onMentionClick = onOpenUser,
                                )
                            }

                            // Series Navigation Card
                            item(key = "series_nav") {
                                d.series?.let { ser ->
                                    IgngTile(soft = true) {
                                        Text(
                                            text = "系列合集 · ${ser.name}（第 ${ser.currentIndex + 1} / ${ser.totalChapters} 篇）",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = colors.accent,
                                        )
                                        Spacer(Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                            ser.prevPost?.let { prev ->
                                                IgngSecondaryButton(
                                                    text = "上一篇",
                                                    onClick = { onOpenPost(prev.id) },
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                            ser.nextPost?.let { next ->
                                                IgngPrimaryButton(
                                                    text = "下一篇",
                                                    onClick = { onOpenPost(next.id) },
                                                    modifier = Modifier.weight(1f),
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Threaded Comments Section
                            item(key = "comments_section") {
                                PostCommentsSection(
                                    container = container,
                                    postId = d.id,
                                    onOpenUser = onOpenUser,
                                    onReportComment = { cmtId ->
                                        if (activeAccount == null) {
                                            onRequireLogin()
                                        } else {
                                            reportTarget = "comment" to cmtId
                                            showReportDialog = true
                                        }
                                    },
                                    onRequireLogin = onRequireLogin,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Auto-hiding Floating Article Action Bar
        if (detail != null && (!detail!!.restrictedWarning || acknowledgedRestriction)) {
            BbsFloatingArticleBar(
                visible = barVisible,
                liked = liked,
                likeCount = likeCount,
                favorited = favorited,
                commentCount = detail!!.commentCount,
                onLike = {
                    if (activeAccount == null) { onRequireLogin(); return@BbsFloatingArticleBar }
                    val prevLiked = liked
                    val prevCount = likeCount
                    liked = !prevLiked
                    likeCount = (prevCount + if (liked) 1 else -1).coerceAtLeast(0)
                    scope.launch {
                        when (val r = container.postRepository.toggleLike(detail!!.id)) {
                            is ApiResult.Success -> { liked = r.data.liked; likeCount = r.data.likeCount }
                            is ApiResult.Failure -> { liked = prevLiked; likeCount = prevCount; feedbackBanner = r.error.message }
                        }
                    }
                },
                onFavorite = {
                    if (activeAccount == null) { onRequireLogin(); return@BbsFloatingArticleBar }
                    val prevFav = favorited
                    favorited = !prevFav
                    scope.launch {
                        when (val r = container.postRepository.toggleFavorite(detail!!.id)) {
                            is ApiResult.Success -> favorited = r.data.favorited
                            is ApiResult.Failure -> { favorited = prevFav; feedbackBanner = r.error.message }
                        }
                    }
                },
                onComment = {
                    scope.launch {
                        listState.animateScrollToItem(commentsAnchorIndex.coerceAtMost(listState.layoutInfo.totalItemsCount.coerceAtLeast(1) - 1))
                    }
                },
                onShare = {
                    val url = detail!!.webUrl ?: container.endpoints.postWebUrl(detail!!.numericId)
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, detail!!.title)
                        putExtra(Intent.EXTRA_TEXT, "${detail!!.title}\n$url")
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "分享文章"))
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }

    // Image Lightbox
    lightboxIndex?.let { idx ->
        IgngImageGalleryLightbox(
            imageUrls = document?.imageUrls.orEmpty(),
            initialIndex = idx,
            onDismiss = { lightboxIndex = null },
        )
    }

    // TOC Bottom Sheet
    if (showTocSheet && document != null) {
        ModalBottomSheet(
            onDismissRequest = { showTocSheet = false },
            containerColor = colors.tile,
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Text("文章目录", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Spacer(Modifier.height(12.dp))
                document.toc.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(IgngRadius.control))
                            .clickable {
                                showTocSheet = false
                                scope.launch { listState.animateScrollToItem(headerItemsCount + entry.blockIndex) }
                            }
                            .padding(start = ((entry.level - 1) * 14).dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
                    ) {
                        Text(entry.title, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // Typography Bottom Sheet
    if (showTypoSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTypoSheet = false },
            containerColor = colors.tile,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("阅读排版设置", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Text("字号：${prefs.reader.fontSizeSp.toInt()} sp", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Slider(
                    value = prefs.reader.fontSizeSp,
                    onValueChange = { v -> scope.launch { container.settingsRepository.updateReaderPrefs { it.copy(fontSizeSp = v) } } },
                    valueRange = 14f..22f,
                )
                Text("行距：${String.format("%.2f", prefs.reader.lineHeightMultiplier)}x", style = MaterialTheme.typography.labelLarge, color = colors.textSecondary)
                Slider(
                    value = prefs.reader.lineHeightMultiplier,
                    onValueChange = { v -> scope.launch { container.settingsRepository.updateReaderPrefs { it.copy(lineHeightMultiplier = v) } } },
                    valueRange = 1.45f..2.0f,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IgngTagChip(
                        text = "默认画布",
                        selected = prefs.reader.canvasTone == ReaderCanvasTone.DEFAULT,
                        onClick = { scope.launch { container.settingsRepository.updateReaderPrefs { it.copy(canvasTone = ReaderCanvasTone.DEFAULT) } } },
                    )
                    IgngTagChip(
                        text = "暖纸护眼",
                        selected = prefs.reader.canvasTone == ReaderCanvasTone.WARM_PAPER,
                        onClick = { scope.launch { container.settingsRepository.updateReaderPrefs { it.copy(canvasTone = ReaderCanvasTone.WARM_PAPER) } } },
                    )
                    IgngTagChip(
                        text = "深墨夜间",
                        selected = prefs.reader.canvasTone == ReaderCanvasTone.INK_DARK,
                        onClick = { scope.launch { container.settingsRepository.updateReaderPrefs { it.copy(canvasTone = ReaderCanvasTone.INK_DARK) } } },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("正文衬线字体", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Switch(
                        checked = prefs.reader.preferSerif,
                        onCheckedChange = { checked ->
                            scope.launch { container.settingsRepository.updateReaderPrefs { it.copy(preferSerif = checked) } }
                        },
                    )
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    // Report Modal with 12-code taxonomy
    if (showReportDialog && reportTarget != null) {
        val reasons = bootstrap?.reportReasons.orEmpty()
        var selectedCode by remember { mutableStateOf(reasons.firstOrNull()?.code ?: "SPAM_AD") }
        var reasonText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("举报违规内容") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        (reasons.takeIf { it.isNotEmpty() } ?: listOf()).forEach { r ->
                            IgngTagChip(
                                text = r.title,
                                selected = selectedCode == r.code,
                                onClick = { selectedCode = r.code },
                            )
                        }
                    }
                    OutlinedTextField(
                        value = reasonText,
                        onValueChange = { reasonText = it },
                        label = { Text("补充说明（选填）") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val (tType, tId) = reportTarget!!
                    showReportDialog = false
                    scope.launch {
                        when (val r = container.postRepository.submitReport(
                            CreateReportRequest(
                                targetType = tType,
                                targetId = tId,
                                reasonCode = selectedCode,
                                reason = reasonText.trim().ifBlank { null },
                            ),
                        )) {
                            is ApiResult.Success -> feedbackBanner = "举报已受理，感谢你协助维护社区秩序。"
                            is ApiResult.Failure -> feedbackBanner = r.error.message
                        }
                    }
                }) { Text("提交举报") }
            },
            dismissButton = { TextButton(onClick = { showReportDialog = false }) { Text("取消") } },
        )
    }
}
