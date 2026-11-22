package net.igng.bbs.feature.column

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.designsystem.BbsPostRowCard
import net.igng.bbs.core.designsystem.IgngAvatar
import net.igng.bbs.core.designsystem.IgngEmptyTile
import net.igng.bbs.core.designsystem.IgngErrorTile
import net.igng.bbs.core.designsystem.IgngInfoBanner
import net.igng.bbs.core.designsystem.IgngLoadingCenter
import net.igng.bbs.core.designsystem.IgngPrimaryButton
import net.igng.bbs.core.designsystem.IgngPullToRefreshBox
import net.igng.bbs.core.designsystem.IgngRadius
import net.igng.bbs.core.designsystem.IgngSecondaryButton
import net.igng.bbs.core.designsystem.IgngSectionHeader
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTile
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.designsystem.IgngTopBar
import net.igng.bbs.core.designsystem.igngBentoEnter
import net.igng.bbs.core.model.ColumnDetailDto
import net.igng.bbs.core.model.ColumnSummaryDto
import net.igng.bbs.core.network.ApiResult

@Composable
fun ColumnSquareScreen(
    container: BbsAppContainer,
    onOpenColumn: (String) -> Unit,
    onOpenCreatorColumns: () -> Unit,
    bottomInsetPadding: PaddingValues = PaddingValues(bottom = 96.dp),
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val bootstrap by container.bootstrap.collectAsState()
    val activeAccount by container.sessionStore.activeAccount.collectAsState()

    var mineOnly by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var columns by remember { mutableStateOf<List<ColumnSummaryDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun load(isRefresh: Boolean) {
        if (isRefresh) refreshing = true else if (columns.isEmpty()) loading = true
        error = null
        when (val res = container.columnRepository.listColumns(
            query = search.trim().ifBlank { null },
            categoryId = selectedCategoryId,
            mine = mineOnly,
        )) {
            is ApiResult.Success -> columns = res.data.data
            is ApiResult.Failure -> if (columns.isEmpty()) error = res.error.message
        }
        loading = false
        refreshing = false
    }

    LaunchedEffect(mineOnly, selectedCategoryId, activeAccount?.userId) { load(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding(),
    ) {
        IgngTopBar(
            title = "专栏广场",
            actions = {
                if (activeAccount != null) {
                    IgngTagChip(text = "管理我的专栏", tone = IgngTone.ACCENT, onClick = onOpenCreatorColumns)
                }
            },
        )
        // Search + All / Mine tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("搜索专栏名称或简介", style = MaterialTheme.typography.bodySmall) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(IgngRadius.pill),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.tile,
                    unfocusedContainerColor = colors.tile,
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.line,
                ),
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = colors.textMuted) },
            )
            IgngTagChip(text = "筛选", tone = IgngTone.ACCENT, onClick = { scope.launch { load(true) } })
        }
        Spacer(Modifier.height(10.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { IgngTagChip(text = "全部专栏", selected = !mineOnly, onClick = { mineOnly = false }) }
            if (activeAccount != null) {
                item { IgngTagChip(text = "我参与的", selected = mineOnly, onClick = { mineOnly = true }) }
            }
            items(bootstrap?.categories.orEmpty(), key = { it.id }) { cat ->
                IgngTagChip(
                    text = cat.name,
                    selected = selectedCategoryId == cat.id,
                    onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id },
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        IgngPullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { scope.launch { load(true) } },
            modifier = Modifier.weight(1f),
        ) {
            when {
                loading && columns.isEmpty() -> IgngLoadingCenter(loading = true)
                error != null && columns.isEmpty() -> Box(Modifier.padding(16.dp)) {
                    IgngErrorTile(message = error!!, onRetry = { scope.launch { load(false) } })
                }
                columns.isEmpty() -> Box(Modifier.padding(16.dp)) {
                    IgngEmptyTile(
                        title = if (mineOnly) "你还没有加入专栏" else "暂无匹配的专栏",
                        subtitle = "探索社区主题专栏，或去创作中心创建自己的专属专栏。",
                    )
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = bottomInsetPadding.calculateBottomPadding() + 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(columns, key = { _, c -> c.id }) { idx, col ->
                        ColumnSummaryCard(
                            column = col,
                            onClick = { onOpenColumn(col.id) },
                            modifier = Modifier.igngBentoEnter(idx),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnSummaryCard(
    column: ColumnSummaryDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    IgngTile(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = column.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IgngTagChip(
                text = when (column.joinMode) {
                    "open" -> "开放加入"
                    "apply" -> "申请加入"
                    else -> "仅受邀"
                },
                tone = if (column.joinMode == "open") IgngTone.SUCCESS else IgngTone.NEUTRAL,
            )
        }
        column.description?.takeIf { it.isNotBlank() }?.let { desc ->
            Spacer(Modifier.height(6.dp))
            Text(desc, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            column.creator?.let { creator ->
                IgngAvatar(creator.avatarUrl, creator.displayName, size = 22.dp)
                Text(creator.displayName, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "${column.postCount} 篇 · ${column.memberCount} 成员 · ${column.subscriberCount} 订阅",
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                color = colors.textMuted,
            )
        }
    }
}

@Composable
fun ColumnDetailScreen(
    container: BbsAppContainer,
    columnIdOrSlug: String,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenColumn: (String) -> Unit,
    onOpenUser: (String) -> Unit,
    onOpenEditorForColumn: (String) -> Unit,
    onRequireLogin: () -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val activeAccount by container.sessionStore.activeAccount.collectAsState()

    var detail by remember { mutableStateOf<ColumnDetailDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var showApplyDialog by remember { mutableStateOf(false) }
    var applyMessage by remember { mutableStateOf("") }
    var showSubDialog by remember { mutableStateOf(false) }
    var subVerification by remember { mutableStateOf("") }

    suspend fun load(isRefresh: Boolean) {
        if (isRefresh) refreshing = true else loading = true
        error = null
        when (val res = container.columnRepository.getColumnDetail(columnIdOrSlug)) {
            is ApiResult.Success -> detail = res.data
            is ApiResult.Failure -> error = res.error.message
        }
        loading = false
        refreshing = false
    }

    LaunchedEffect(columnIdOrSlug, activeAccount?.userId) { load(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding(),
    ) {
        IgngTopBar(
            title = detail?.name ?: "专栏详情",
            subtitle = detail?.slug?.let { "/columns/$it" },
            onBack = onBack,
            actions = {
                if (detail?.viewerState?.canPost == true) {
                    IgngTagChip(
                        text = "投稿发文",
                        tone = IgngTone.ACCENT,
                        onClick = { onOpenEditorForColumn(detail!!.id) },
                    )
                }
            },
        )

        IgngPullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { scope.launch { load(true) } },
            modifier = Modifier.weight(1f),
        ) {
            when {
                loading && detail == null -> IgngLoadingCenter(loading = true)
                error != null && detail == null -> Box(Modifier.padding(16.dp)) {
                    IgngErrorTile(message = error!!, onRetry = { scope.launch { load(false) } })
                }
                detail != null -> {
                    val col = detail!!
                    val vs = col.viewerState
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        feedback?.let { msg ->
                            item(key = "feedback") {
                                IgngInfoBanner(title = "操作结果", message = msg, tone = IgngTone.SUCCESS)
                            }
                        }

                        // Parent breadcrumbs
                        if (col.breadcrumbs.isNotEmpty()) {
                            item(key = "breadcrumbs") {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(col.breadcrumbs, key = { it.id }) { parent ->
                                        IgngTagChip(
                                            text = "上级：${parent.name}",
                                            tone = IgngTone.NEUTRAL,
                                            onClick = { onOpenColumn(parent.id) },
                                        )
                                    }
                                }
                            }
                        }

                        // Hero Bento tile
                        item(key = "hero") {
                            IgngTile {
                                Text(col.name, style = MaterialTheme.typography.headlineSmall, color = colors.textPrimary)
                                col.description?.takeIf { it.isNotBlank() }?.let { desc ->
                                    Spacer(Modifier.height(6.dp))
                                    Text(desc, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                                }
                                Spacer(Modifier.height(10.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("${col.postCount} 篇文章", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                                    Text("${col.memberCount} 位成员", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                                    Text("${col.subscriberCount} 位订阅者", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                                }
                                Spacer(Modifier.height(12.dp))
                                // Membership & Subscription action buttons
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    if (!vs.isCreator) {
                                        when {
                                            vs.isBlacklisted -> IgngTagChip(text = "已被专栏限制", tone = IgngTone.DANGER)
                                            vs.isMember -> IgngSecondaryButton(
                                                text = "退出专栏",
                                                onClick = {
                                                    scope.launch {
                                                        when (val r = container.columnRepository.leaveColumn(col.id)) {
                                                            is ApiResult.Success -> { feedback = "已退出专栏"; load(true) }
                                                            is ApiResult.Failure -> feedback = r.error.message
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                            )
                                            vs.pendingApply -> IgngTagChip(text = "加入申请审核中", tone = IgngTone.WARNING)
                                            col.joinMode == "open" -> IgngPrimaryButton(
                                                text = "加入专栏",
                                                onClick = {
                                                    if (activeAccount == null) { onRequireLogin(); return@IgngPrimaryButton }
                                                    scope.launch {
                                                        when (val r = container.columnRepository.joinColumn(col.id)) {
                                                            is ApiResult.Success -> { feedback = r.data.message.ifBlank { "已加入专栏" }; load(true) }
                                                            is ApiResult.Failure -> feedback = r.error.message
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                            )
                                            col.joinMode == "apply" -> IgngPrimaryButton(
                                                text = "申请加入",
                                                onClick = {
                                                    if (activeAccount == null) onRequireLogin() else showApplyDialog = true
                                                },
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                    }

                                    // Subscription button
                                    val subStatus = vs.subscriptionStatus
                                    if (!vs.isCreator && !vs.isBlacklisted) {
                                        if (subStatus == "approved" || subStatus == "pending") {
                                            IgngSecondaryButton(
                                                text = if (subStatus == "pending") "取消订阅申请" else "已订阅",
                                                onClick = {
                                                    scope.launch {
                                                        when (val r = container.columnRepository.unsubscribeColumn(col.id)) {
                                                            is ApiResult.Success -> { feedback = "已取消订阅"; load(true) }
                                                            is ApiResult.Failure -> feedback = r.error.message
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                            )
                                        } else {
                                            IgngSecondaryButton(
                                                text = "订阅专栏",
                                                onClick = {
                                                    if (activeAccount == null) { onRequireLogin(); return@IgngSecondaryButton }
                                                    if (col.subscriptionMode == "direct") {
                                                        scope.launch {
                                                            when (val r = container.columnRepository.subscribeColumn(col.id)) {
                                                                is ApiResult.Success -> { feedback = "订阅成功"; load(true) }
                                                                is ApiResult.Failure -> feedback = r.error.message
                                                            }
                                                        }
                                                    } else {
                                                        showSubDialog = true
                                                    }
                                                },
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Sub-columns
                        if (col.subColumns.isNotEmpty()) {
                            item(key = "sub_header") { IgngSectionHeader(title = "子专栏（${col.subColumns.size}）") }
                            items(col.subColumns, key = { "sub_${it.id}" }) { sub ->
                                ColumnSummaryCard(column = sub, onClick = { onOpenColumn(sub.id) })
                            }
                        }

                        // Pinned posts
                        if (col.pinnedPosts.isNotEmpty()) {
                            item(key = "pin_header") { IgngSectionHeader(title = "置顶文章") }
                            items(col.pinnedPosts, key = { "pin_${it.id}" }) { post ->
                                BbsPostRowCard(
                                    post = post,
                                    onClick = { onOpenPost(post.id) },
                                    onAuthorClick = post.author?.id?.let { uid -> { onOpenUser(uid) } },
                                )
                            }
                        }

                        // Regular posts
                        item(key = "posts_header") { IgngSectionHeader(title = "专栏文章") }
                        if (!vs.canViewPosts) {
                            item(key = "restricted") {
                                IgngEmptyTile(
                                    title = "仅限成员或订阅者查看",
                                    subtitle = "当前专栏设置了访问范围，加入或订阅通过后即可阅读全部文章。",
                                )
                            }
                        } else if (col.posts.isEmpty()) {
                            item(key = "no_posts") {
                                IgngEmptyTile(title = "暂无专栏文章", subtitle = "这个专栏还没有发布文章。")
                            }
                        } else {
                            items(col.posts, key = { "post_${it.id}" }) { post ->
                                BbsPostRowCard(
                                    post = post,
                                    onClick = { onOpenPost(post.id) },
                                    onAuthorClick = post.author?.id?.let { uid -> { onOpenUser(uid) } },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showApplyDialog && detail != null) {
        AlertDialog(
            onDismissRequest = { showApplyDialog = false },
            title = { Text("申请加入「${detail!!.name}」") },
            text = {
                OutlinedTextField(
                    value = applyMessage,
                    onValueChange = { applyMessage = it },
                    label = { Text("申请理由（选填）") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val colId = detail!!.id
                    showApplyDialog = false
                    scope.launch {
                        when (val r = container.columnRepository.joinColumn(colId, applyMessage.trim().ifBlank { null })) {
                            is ApiResult.Success -> { feedback = r.data.message.ifBlank { "已提交申请" }; applyMessage = ""; load(true) }
                            is ApiResult.Failure -> feedback = r.error.message
                        }
                    }
                }) { Text("提交申请") }
            },
            dismissButton = { TextButton(onClick = { showApplyDialog = false }) { Text("取消") } },
        )
    }

    if (showSubDialog && detail != null) {
        val isSecret = detail!!.subscriptionMode == "verification"
        AlertDialog(
            onDismissRequest = { showSubDialog = false },
            title = { Text(if (isSecret) "输入订阅口令" else "提交订阅申请") },
            text = {
                OutlinedTextField(
                    value = subVerification,
                    onValueChange = { subVerification = it },
                    label = { Text(if (isSecret) "验证口令" else "订阅附言") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val colId = detail!!.id
                    showSubDialog = false
                    scope.launch {
                        val r = if (isSecret) {
                            container.columnRepository.subscribeColumn(colId, verification = subVerification.trim())
                        } else {
                            container.columnRepository.subscribeColumn(colId, message = subVerification.trim())
                        }
                        when (r) {
                            is ApiResult.Success -> { feedback = "已提交订阅（状态：${r.data.status}）"; subVerification = ""; load(true) }
                            is ApiResult.Failure -> feedback = r.error.message
                        }
                    }
                }) { Text("确认") }
            },
            dismissButton = { TextButton(onClick = { showSubDialog = false }) { Text("取消") } },
        )
    }
}
