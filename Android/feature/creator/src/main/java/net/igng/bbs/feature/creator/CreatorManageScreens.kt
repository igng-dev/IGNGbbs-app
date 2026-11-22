package net.igng.bbs.feature.creator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.data.CreateAppealRequest
import net.igng.bbs.core.data.CreateCreatorColumnRequest
import net.igng.bbs.core.data.CreateSeriesRequest
import net.igng.bbs.core.data.CreatorColumnActionRequest
import net.igng.bbs.core.data.CreatorColumnDetailDto
import net.igng.bbs.core.data.InviteCollaboratorRequest
import net.igng.bbs.core.data.UpdateSeriesRequest
import net.igng.bbs.core.designsystem.BbsRiskMeterCard
import net.igng.bbs.core.designsystem.ChartPoint
import net.igng.bbs.core.designsystem.IgngEmptyTile
import net.igng.bbs.core.designsystem.IgngErrorTile
import net.igng.bbs.core.designsystem.IgngHorizontalBarList
import net.igng.bbs.core.designsystem.IgngInfoBanner
import net.igng.bbs.core.designsystem.IgngLineChart
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
import net.igng.bbs.core.model.BbsFormatters
import net.igng.bbs.core.model.ColumnEventDto
import net.igng.bbs.core.model.ColumnSummaryDto
import net.igng.bbs.core.model.CreatorCommentDto
import net.igng.bbs.core.model.CreatorInvitesDto
import net.igng.bbs.core.model.CreatorMediaListDto
import net.igng.bbs.core.model.CreatorReviewsResponseDto
import net.igng.bbs.core.model.CreatorSeriesDetailDto
import net.igng.bbs.core.model.PostAnalyticsDto
import net.igng.bbs.core.model.PostSummaryDto
import net.igng.bbs.core.model.PostVersionDto
import net.igng.bbs.core.model.SeriesSummaryDto
import net.igng.bbs.core.network.ApiResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorPostsScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
    onNewArticle: () -> Unit,
    onEditPost: (String) -> Unit,
    onOpenPost: (String) -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf("all") }
    var posts by remember { mutableStateOf<List<PostSummaryDto>>(emptyList()) }
    var invites by remember { mutableStateOf<CreatorInvitesDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    var analyticsData by remember { mutableStateOf<PostAnalyticsDto?>(null) }
    var versionsPostId by remember { mutableStateOf<String?>(null) }
    var versionsList by remember { mutableStateOf<List<PostVersionDto>>(emptyList()) }
    var inviteTargetPostId by remember { mutableStateOf<String?>(null) }
    var inviteUserId by remember { mutableStateOf("") }
    var inviteRoleTitle by remember { mutableStateOf("联合作者") }

    suspend fun load(isRefresh: Boolean) {
        if (isRefresh) refreshing = true else if (posts.isEmpty()) loading = true
        error = null
        when (val res = container.creatorRepository.listPosts(filter = filter, pageSize = 30)) {
            is ApiResult.Success -> posts = res.data.data
            is ApiResult.Failure -> if (posts.isEmpty()) error = res.error.message
        }
        when (val inv = container.creatorRepository.listInvites()) {
            is ApiResult.Success -> invites = inv.data
            is ApiResult.Failure -> Unit
        }
        loading = false
        refreshing = false
    }

    LaunchedEffect(filter) { load(false) }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(
            title = "文章与协作管理",
            onBack = onBack,
            actions = { IgngTagChip(text = "+ 写文章", tone = IgngTone.ACCENT, onClick = onNewArticle) },
        )
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(
                listOf("all" to "全部", "published" to "已发布", "draft" to "草稿", "pending" to "审核中", "rejected" to "未通过", "collab" to "参与协作"),
                key = { it.first },
            ) { (k, label) ->
                IgngTagChip(text = label, selected = filter == k, onClick = { filter = k })
            }
        }
        Spacer(Modifier.height(8.dp))

        IgngPullToRefreshBox(isRefreshing = refreshing, onRefresh = { scope.launch { load(true) } }, modifier = Modifier.weight(1f)) {
            when {
                loading && posts.isEmpty() -> IgngLoadingCenter(loading = true)
                error != null && posts.isEmpty() -> Box(Modifier.padding(16.dp)) {
                    IgngErrorTile(message = error!!, onRetry = { scope.launch { load(false) } })
                }
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    notice?.let { msg -> item { IgngInfoBanner("提示", msg, IgngTone.ACCENT) } }

                    val pendingReceived = invites?.received?.filter { it.status == "pending" }.orEmpty()
                    if (pendingReceived.isNotEmpty()) {
                        item { IgngSectionHeader(title = "收到的协作邀请（${pendingReceived.size}）") }
                        items(pendingReceived, key = { it.id }) { inv ->
                            IgngTile(soft = true) {
                                Text("《${inv.postTitle}》 · ${inv.roleTitle}", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                Text("来自 ${inv.counterparty?.displayName ?: "作者"} · 权限：${inv.permission}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IgngTagChip(text = "接受邀请", tone = IgngTone.SUCCESS, onClick = {
                                        scope.launch {
                                            when (val r = container.creatorRepository.respondInvite(inv.id, "accept")) {
                                                is ApiResult.Success -> { notice = "已接受协作邀请"; load(true) }
                                                is ApiResult.Failure -> notice = r.error.message
                                            }
                                        }
                                    })
                                    IgngTagChip(text = "拒绝", tone = IgngTone.DANGER, onClick = {
                                        scope.launch {
                                            when (val r = container.creatorRepository.respondInvite(inv.id, "reject")) {
                                                is ApiResult.Success -> { notice = "已拒绝邀请"; load(true) }
                                                is ApiResult.Failure -> notice = r.error.message
                                            }
                                        }
                                    })
                                }
                            }
                        }
                    }

                    if (posts.isEmpty()) {
                        item { IgngEmptyTile("该分类下暂无文章", "点击右上角即可开始写作。") }
                    } else {
                        items(posts, key = { it.id }) { post ->
                            IgngTile {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(post.title, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
                                    IgngTagChip(
                                        text = BbsFormatters.postStatusLabel(post.status, post.banStatus, post.moderationStatus),
                                        tone = when (post.moderationStatus) {
                                            "pending" -> IgngTone.WARNING
                                            "rejected" -> IgngTone.DANGER
                                            else -> IgngTone.NEUTRAL
                                        },
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "${BbsFormatters.compactCount(post.views)} 阅读 · ${post.likeCount} 赞 · ${post.commentCount} 评论 · ${BbsFormatters.relativeTime(post.updatedAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                )
                                post.rejectionReason?.takeIf { it.isNotBlank() }?.let { reason ->
                                    Spacer(Modifier.height(4.dp))
                                    Text("审核意见：$reason", style = MaterialTheme.typography.bodySmall, color = colors.danger)
                                }
                                Spacer(Modifier.height(10.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    item { IgngTagChip("编辑", tone = IgngTone.ACCENT, onClick = { onEditPost(post.id) }) }
                                    item { IgngTagChip("阅读", onClick = { onOpenPost(post.id) }) }
                                    item {
                                        IgngTagChip("数据", onClick = {
                                            scope.launch {
                                                when (val r = container.creatorRepository.getPostAnalytics(post.id, 30)) {
                                                    is ApiResult.Success -> analyticsData = r.data
                                                    is ApiResult.Failure -> notice = r.error.message
                                                }
                                            }
                                        })
                                    }
                                    item {
                                        IgngTagChip("历史版本", onClick = {
                                            scope.launch {
                                                when (val r = container.creatorRepository.listVersions(post.id)) {
                                                    is ApiResult.Success -> { versionsPostId = post.id; versionsList = r.data.data }
                                                    is ApiResult.Failure -> notice = r.error.message
                                                }
                                            }
                                        })
                                    }
                                    item { IgngTagChip("邀请协作", onClick = { inviteTargetPostId = post.id }) }
                                    if (post.moderationStatus == "pending") {
                                        item {
                                            IgngTagChip("撤回送审", tone = IgngTone.WARNING, onClick = {
                                                scope.launch {
                                                    when (val r = container.creatorRepository.withdrawSubmission(post.id)) {
                                                        is ApiResult.Success -> { notice = "已撤回本次送审"; load(true) }
                                                        is ApiResult.Failure -> notice = r.error.message
                                                    }
                                                }
                                            })
                                        }
                                    }
                                    item {
                                        IgngTagChip("删除", tone = IgngTone.DANGER, onClick = {
                                            scope.launch {
                                                when (val r = container.creatorRepository.deletePost(post.id)) {
                                                    is ApiResult.Success -> { notice = "文章已删除"; load(true) }
                                                    is ApiResult.Failure -> notice = r.error.message
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    analyticsData?.let { stat ->
        ModalBottomSheet(onDismissRequest = { analyticsData = null }, containerColor = colors.tile) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("数据透视 · ${stat.title}", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Text("累计阅读 ${stat.lifetimeViews} · 近 ${stat.windowDays} 天 ${stat.totals.views}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                IgngLineChart(points = stat.daily.map { ChartPoint(it.date, it.totalViews) })
                IgngHorizontalBarList(
                    items = listOf(
                        "站内跳转" to stat.totals.internalRef,
                        "搜索引擎" to stat.totals.searchRef,
                        "外部链接" to stat.totals.externalRef,
                    ),
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    versionsPostId?.let { pid ->
        ModalBottomSheet(onDismissRequest = { versionsPostId = null }, containerColor = colors.tile) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("历史已发布版本（${versionsList.size}）", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                if (versionsList.isEmpty()) {
                    Text("这篇文章尚无历史发布快照。", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                } else {
                    versionsList.forEach { rev ->
                        IgngTile(soft = true, padding = PaddingValues(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("v${rev.revisionNo} · ${rev.title}", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                    Text(BbsFormatters.fullDateTime(rev.publishedAt), style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                                }
                                IgngTagChip("恢复到草稿", tone = IgngTone.ACCENT, onClick = {
                                    scope.launch {
                                        when (val r = container.creatorRepository.restoreVersion(pid, rev.revisionNo)) {
                                            is ApiResult.Success -> { notice = "已恢复版本 v${rev.revisionNo} 到草稿"; versionsPostId = null }
                                            is ApiResult.Failure -> notice = r.error.message
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    inviteTargetPostId?.let { pid ->
        AlertDialog(
            onDismissRequest = { inviteTargetPostId = null },
            title = { Text("邀请协作者") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = inviteUserId, onValueChange = { inviteUserId = it }, label = { Text("目标账户编号（如 usr_12 或 12）") })
                    OutlinedTextField(value = inviteRoleTitle, onValueChange = { inviteRoleTitle = it }, label = { Text("署名角色（如 联合作者 / 校对）") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val uid = inviteUserId.trim()
                    if (uid.isEmpty()) return@TextButton
                    inviteTargetPostId = null
                    scope.launch {
                        when (val r = container.creatorRepository.inviteCollaborator(pid, InviteCollaboratorRequest(uid, inviteRoleTitle.trim().ifBlank { "联合作者" }))) {
                            is ApiResult.Success -> { notice = "协作邀请已发出"; inviteUserId = "" }
                            is ApiResult.Failure -> notice = r.error.message
                        }
                    }
                }) { Text("发送邀请") }
            },
            dismissButton = { TextButton(onClick = { inviteTargetPostId = null }) { Text("取消") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorColumnsScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
    onOpenColumn: (String) -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    var columns by remember { mutableStateOf<List<ColumnSummaryDto>>(emptyList()) }
    var events by remember { mutableStateOf<List<ColumnEventDto>>(emptyList()) }
    var manageDetail by remember { mutableStateOf<CreatorColumnDetailDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var notice by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newSlug by remember { mutableStateOf("") }
    var newDesc by remember { mutableStateOf("") }

    suspend fun load() {
        loading = true
        when (val c = container.creatorRepository.listColumns()) {
            is ApiResult.Success -> columns = c.data.data
            is ApiResult.Failure -> notice = c.error.message
        }
        when (val ev = container.creatorRepository.listColumnEvents()) {
            is ApiResult.Success -> events = ev.data.data
            is ApiResult.Failure -> Unit
        }
        loading = false
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(
            title = "专栏管理与事件",
            onBack = onBack,
            actions = { IgngTagChip("+ 新建专栏", tone = IgngTone.ACCENT, onClick = { showCreate = true }) },
        )
        if (loading && columns.isEmpty()) {
            IgngLoadingCenter(loading = true)
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                notice?.let { msg -> item { IgngInfoBanner("提示", msg, IgngTone.ACCENT) } }

                val pendingEvents = events.filter { it.canRespond }
                if (pendingEvents.isNotEmpty()) {
                    item { IgngSectionHeader("待回应专栏申请与邀请（${pendingEvents.size}）") }
                    items(pendingEvents, key = { it.id }) { ev ->
                        IgngTile(soft = true) {
                            Text("${ev.column?.name ?: "专栏"} · ${ev.type}", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                            ev.message?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary) }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IgngTagChip("同意", tone = IgngTone.SUCCESS, onClick = {
                                    scope.launch {
                                        when (val r = container.creatorRepository.respondColumnEvent(ev.id, "accept")) {
                                            is ApiResult.Success -> { notice = r.data.message ?: "已同意"; load() }
                                            is ApiResult.Failure -> notice = r.error.message
                                        }
                                    }
                                })
                                IgngTagChip("拒绝", tone = IgngTone.DANGER, onClick = {
                                    scope.launch {
                                        when (val r = container.creatorRepository.respondColumnEvent(ev.id, "reject")) {
                                            is ApiResult.Success -> { notice = r.data.message ?: "已拒绝"; load() }
                                            is ApiResult.Failure -> notice = r.error.message
                                        }
                                    }
                                })
                            }
                        }
                    }
                }

                item { IgngSectionHeader("我管理的专栏（${columns.size}）") }
                if (columns.isEmpty()) {
                    item { IgngEmptyTile("还没有专栏", "创建一个主题专栏来组织和汇聚系列文章。") }
                } else {
                    items(columns, key = { it.id }) { col ->
                        IgngTile {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(col.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                IgngTagChip(text = col.myRole ?: "member", tone = IgngTone.ACCENT)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${col.postCount} 篇 · ${col.memberCount} 成员 · ${col.subscriberCount} 订阅", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IgngTagChip("查看专栏", onClick = { onOpenColumn(col.id) })
                                if (col.myRole == "creator" || col.myRole == "manager") {
                                    IgngTagChip("管理审批", tone = IgngTone.ACCENT, onClick = {
                                        scope.launch {
                                            when (val r = container.creatorRepository.getColumnDetail(col.id)) {
                                                is ApiResult.Success -> manageDetail = r.data
                                                is ApiResult.Failure -> notice = r.error.message
                                            }
                                        }
                                    })
                                }
                                if (col.myRole == "creator") {
                                    IgngTagChip("删除", tone = IgngTone.DANGER, onClick = {
                                        scope.launch {
                                            when (val r = container.creatorRepository.deleteColumn(col.id)) {
                                                is ApiResult.Success -> { notice = "专栏已删除"; load() }
                                                is ApiResult.Failure -> notice = r.error.message
                                            }
                                        }
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    manageDetail?.let { detail ->
        ModalBottomSheet(onDismissRequest = { manageDetail = null }, containerColor = colors.tile) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("专栏管理 · ${detail.name}", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                Text("成员 ${detail.members.size} 人 · 管理员 ${detail.managers.size} 人 · 订阅记录 ${detail.subscriptions.size} 条", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                val pendingSubs = detail.subscriptions.filter { it.status == "pending" }
                if (pendingSubs.isNotEmpty()) {
                    Text("待审订阅", style = MaterialTheme.typography.titleSmall, color = colors.warning)
                    pendingSubs.forEach { sub ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(sub.user.displayName, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IgngTagChip("批准", tone = IgngTone.SUCCESS, onClick = {
                                    scope.launch {
                                        container.creatorRepository.performColumnAction(detail.id, CreatorColumnActionRequest("review_subscription", subscriptionId = sub.id, approved = true))
                                        manageDetail = null; load()
                                    }
                                })
                                IgngTagChip("拒绝", tone = IgngTone.DANGER, onClick = {
                                    scope.launch {
                                        container.creatorRepository.performColumnAction(detail.id, CreatorColumnActionRequest("review_subscription", subscriptionId = sub.id, approved = false))
                                        manageDetail = null; load()
                                    }
                                })
                            }
                        }
                    }
                }
                if (detail.blacklist.isNotEmpty()) {
                    Text("专栏黑名单", style = MaterialTheme.typography.titleSmall, color = colors.danger)
                    detail.blacklist.forEach { b ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(b.user.displayName, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
                            IgngTagChip("移出黑名单", onClick = {
                                scope.launch {
                                    container.creatorRepository.performColumnAction(detail.id, CreatorColumnActionRequest("unblacklist", targetUserId = b.user.id))
                                    manageDetail = null; load()
                                }
                            })
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("创建新专栏") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("专栏名称") })
                    OutlinedTextField(value = newSlug, onValueChange = { newSlug = it }, label = { Text("英文标识 slug（如 dev-notes）") })
                    OutlinedTextField(value = newDesc, onValueChange = { newDesc = it }, label = { Text("简介（选填）") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newName.isBlank() || newSlug.isBlank()) return@TextButton
                    showCreate = false
                    scope.launch {
                        when (val r = container.creatorRepository.createColumn(CreateCreatorColumnRequest(newName.trim(), newSlug.trim(), newDesc.trim().ifBlank { null }))) {
                            is ApiResult.Success -> { notice = "专栏已创建"; newName = ""; newSlug = ""; newDesc = ""; load() }
                            is ApiResult.Failure -> notice = r.error.message
                        }
                    }
                }) { Text("创建") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("取消") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorSeriesScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    var seriesList by remember { mutableStateOf<List<SeriesSummaryDto>>(emptyList()) }
    var activeSeries by remember { mutableStateOf<CreatorSeriesDetailDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var notice by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var slug by remember { mutableStateOf("") }

    suspend fun load() {
        loading = true
        when (val r = container.creatorRepository.listSeries()) {
            is ApiResult.Success -> seriesList = r.data.data
            is ApiResult.Failure -> notice = r.error.message
        }
        loading = false
    }

    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(
            title = "系列合集编排",
            onBack = onBack,
            actions = { IgngTagChip("+ 新建系列", tone = IgngTone.ACCENT, onClick = { showCreate = true }) },
        )
        if (loading && seriesList.isEmpty()) {
            IgngLoadingCenter(loading = true)
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                notice?.let { item { IgngInfoBanner("提示", it, IgngTone.ACCENT) } }
                if (seriesList.isEmpty()) {
                    item { IgngEmptyTile("还没有系列合集", "把多篇连载或教程文章编排为一个系列，方便读者连续阅读。") }
                } else {
                    items(seriesList, key = { it.id }) { ser ->
                        IgngTile {
                            Text(ser.name, style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                            Text("${ser.postCount} 篇章节 · ${ser.views} 次浏览", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IgngTagChip("章节排序与趋势", tone = IgngTone.ACCENT, onClick = {
                                    scope.launch {
                                        when (val d = container.creatorRepository.getSeriesDetail(ser.id)) {
                                            is ApiResult.Success -> activeSeries = d.data
                                            is ApiResult.Failure -> notice = d.error.message
                                        }
                                    }
                                })
                                IgngTagChip("删除系列", tone = IgngTone.DANGER, onClick = {
                                    scope.launch {
                                        when (val d = container.creatorRepository.deleteSeries(ser.id)) {
                                            is ApiResult.Success -> { notice = "系列已删除"; load() }
                                            is ApiResult.Failure -> notice = d.error.message
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    activeSeries?.let { detail ->
        ModalBottomSheet(onDismissRequest = { activeSeries = null }, containerColor = colors.tile) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("章节编排 · ${detail.name}", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                if (detail.viewsTrend.isNotEmpty()) {
                    IgngLineChart(points = detail.viewsTrend.map { ChartPoint(it.date, it.views) })
                }
                if (detail.posts.isEmpty()) {
                    Text("当前系列暂无收录文章。", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                } else {
                    detail.posts.forEachIndexed { idx, entry ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("${idx + 1}. ${entry.post.title}", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
                            IconButton(enabled = idx > 0, onClick = {
                                val ids = detail.posts.map { it.post.id }.toMutableList()
                                val item = ids.removeAt(idx)
                                ids.add(idx - 1, item)
                                scope.launch {
                                    when (val r = container.creatorRepository.updateSeries(detail.id, UpdateSeriesRequest(postIds = ids))) {
                                        is ApiResult.Success -> activeSeries = r.data
                                        is ApiResult.Failure -> notice = r.error.message
                                    }
                                }
                            }) { Icon(Icons.Rounded.ArrowUpward, contentDescription = "上移") }
                            IconButton(enabled = idx < detail.posts.lastIndex, onClick = {
                                val ids = detail.posts.map { it.post.id }.toMutableList()
                                val item = ids.removeAt(idx)
                                ids.add(idx + 1, item)
                                scope.launch {
                                    when (val r = container.creatorRepository.updateSeries(detail.id, UpdateSeriesRequest(postIds = ids))) {
                                        is ApiResult.Success -> activeSeries = r.data
                                        is ApiResult.Failure -> notice = r.error.message
                                    }
                                }
                            }) { Icon(Icons.Rounded.ArrowDownward, contentDescription = "下移") }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("新建系列合集") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("系列名称") })
                    OutlinedTextField(value = slug, onValueChange = { slug = it }, label = { Text("英文标识 slug") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isBlank() || slug.isBlank()) return@TextButton
                    showCreate = false
                    scope.launch {
                        when (val r = container.creatorRepository.createSeries(CreateSeriesRequest(name.trim(), slug.trim()))) {
                            is ApiResult.Success -> { notice = "系列已创建"; name = ""; slug = ""; load() }
                            is ApiResult.Failure -> notice = r.error.message
                        }
                    }
                }) { Text("创建") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("取消") } },
        )
    }
}

@Composable
fun CreatorCommentsScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    var comments by remember { mutableStateOf<List<CreatorCommentDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var notice by remember { mutableStateOf<String?>(null) }

    suspend fun load() {
        loading = true
        when (val r = container.creatorRepository.listComments(pageSize = 40)) {
            is ApiResult.Success -> comments = r.data.data
            is ApiResult.Failure -> notice = r.error.message
        }
        loading = false
    }
    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(title = "收到的评论管理", onBack = onBack)
        if (loading && comments.isEmpty()) {
            IgngLoadingCenter(loading = true)
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                notice?.let { item { IgngInfoBanner("提示", it, IgngTone.ACCENT) } }
                if (comments.isEmpty()) {
                    item { IgngEmptyTile("暂无评论", "你的文章收到新评论后会集中展示在这里。") }
                } else {
                    items(comments, key = { it.id }) { c ->
                        IgngTile(onClick = { c.postId?.let(onOpenPost) }) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${c.author?.displayName ?: "用户"} 评论了《${c.postTitle}》", style = MaterialTheme.typography.labelLarge, color = colors.accent, modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    scope.launch {
                                        when (val r = container.postRepository.deleteComment(c.id)) {
                                            is ApiResult.Success -> { notice = "已删除该评论"; load() }
                                            is ApiResult.Failure -> notice = r.error.message
                                        }
                                    }
                                }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "删除", tint = colors.danger, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(c.content, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                            Spacer(Modifier.height(4.dp))
                            Text(BbsFormatters.relativeTime(c.createdAt), style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatorReviewsScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val bootstrap by container.bootstrap.collectAsState()
    var data by remember { mutableStateOf<CreatorReviewsResponseDto?>(null) }
    var includePassed by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var notice by remember { mutableStateOf<String?>(null) }
    var appealTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    var appealGround by remember { mutableStateOf("CONTEXT_MISREAD") }
    var appealReason by remember { mutableStateOf("") }

    suspend fun load() {
        loading = true
        when (val r = container.creatorRepository.listReviews(includePassed = includePassed)) {
            is ApiResult.Success -> data = r.data
            is ApiResult.Failure -> notice = r.error.message
        }
        loading = false
    }
    LaunchedEffect(includePassed) { load() }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(
            title = "审核与申诉中心",
            onBack = onBack,
            actions = {
                IgngTagChip(text = if (includePassed) "含已通过" else "仅待处理/驳回", selected = includePassed, onClick = { includePassed = !includePassed })
            },
        )
        if (loading && data == null) {
            IgngLoadingCenter(loading = true)
        } else if (data != null) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                notice?.let { item { IgngInfoBanner("提示", it, IgngTone.ACCENT) } }
                item { BbsRiskMeterCard(risk = data!!.risk) }
                item { IgngSectionHeader("审核记录（${data!!.reviews.size}）") }
                if (data!!.reviews.isEmpty()) {
                    item { IgngEmptyTile("暂无异常审核记录", "你近期提交的内容均正常通过或尚未提交。") }
                } else {
                    items(data!!.reviews, key = { it.id }) { rev ->
                        IgngTile {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(rev.targetTitle, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary, modifier = Modifier.weight(1f))
                                IgngTagChip(
                                    text = rev.outcome ?: rev.status,
                                    tone = if (rev.outcome == "rejected") IgngTone.DANGER else if (rev.outcome == "approved") IgngTone.SUCCESS else IgngTone.WARNING,
                                )
                            }
                            rev.rejectionReason?.takeIf { it.isNotBlank() }?.let {
                                Spacer(Modifier.height(4.dp))
                                Text("原因：$it", style = MaterialTheme.typography.bodySmall, color = colors.danger)
                            }
                            rev.suggestion?.takeIf { it.isNotBlank() }?.let {
                                Spacer(Modifier.height(2.dp))
                                Text("修改建议：$it", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            }
                            if (rev.outcome == "rejected" && !rev.hasPendingAppeal) {
                                Spacer(Modifier.height(8.dp))
                                IgngTagChip("发起申诉", tone = IgngTone.ACCENT, onClick = { appealTarget = rev.targetType to rev.targetId })
                            }
                        }
                    }
                }
                if (data!!.appeals.isNotEmpty()) {
                    item { IgngSectionHeader("我的申诉记录（${data!!.appeals.size}）") }
                    items(data!!.appeals, key = { it.id }) { apl ->
                        IgngTile(soft = true) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("第 ${apl.round}/${apl.maxRounds} 轮申诉 · ${apl.targetId}", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                IgngTagChip(text = apl.status, tone = if (apl.status == "approved") IgngTone.SUCCESS else if (apl.status == "rejected") IgngTone.DANGER else IgngTone.WARNING)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("申诉陈述：${apl.reason}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            apl.reply?.takeIf { it.isNotBlank() }?.let {
                                Spacer(Modifier.height(4.dp))
                                Text("复核答复：$it", style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
                            }
                            apl.arguments.forEach { arg ->
                                Text("· [${if (arg.accepted) "采纳" else "未采纳"}] ${arg.point}：${arg.reply}", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    appealTarget?.let { (tType, tId) ->
        AlertDialog(
            onDismissRequest = { appealTarget = null },
            title = { Text("提交内容复核申诉") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        (bootstrap?.appealGrounds.orEmpty()).forEach { g ->
                            IgngTagChip(text = g.label, selected = appealGround == g.code, onClick = { appealGround = g.code })
                        }
                    }
                    OutlinedTextField(
                        value = appealReason,
                        onValueChange = { appealReason = it },
                        label = { Text("请说明具体申诉理由与上下文") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (appealReason.trim().length < 4) return@TextButton
                    appealTarget = null
                    scope.launch {
                        when (val r = container.creatorRepository.submitAppeal(CreateAppealRequest(tType, tId, appealReason.trim(), appealGround))) {
                            is ApiResult.Success -> { notice = "申诉已提交（第 ${r.data.round}/${r.data.maxRounds} 轮）"; appealReason = ""; load() }
                            is ApiResult.Failure -> notice = r.error.message
                        }
                    }
                }) { Text("提交申诉") }
            },
            dismissButton = { TextButton(onClick = { appealTarget = null }) { Text("取消") } },
        )
    }
}

@Composable
fun CreatorMediaScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    var mediaList by remember { mutableStateOf<CreatorMediaListDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var uploading by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }
    var appealMediaId by remember { mutableStateOf<String?>(null) }
    var appealReason by remember { mutableStateOf("") }

    suspend fun load() {
        loading = true
        when (val r = container.creatorRepository.listMedia(pageSize = 40)) {
            is ApiResult.Success -> mediaList = r.data
            is ApiResult.Failure -> notice = r.error.message
        }
        loading = false
    }
    LaunchedEffect(Unit) { load() }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            uploading = true
            val data = withContext(Dispatchers.IO) {
                runCatching {
                    val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@runCatching null
                    Triple(bytes, mime, "upload_${System.currentTimeMillis()}.jpg")
                }.getOrNull()
            }
            if (data != null) {
                when (val r = container.creatorRepository.uploadMedia(data.third, data.second, data.first)) {
                    is ApiResult.Success -> { notice = "图片上传成功"; load() }
                    is ApiResult.Failure -> notice = r.error.message
                }
            }
            uploading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(
            title = "云端媒体资产",
            onBack = onBack,
            actions = {
                IgngPrimaryButton(
                    text = if (uploading) "上传中…" else "上传图片",
                    icon = Icons.Rounded.Upload,
                    enabled = !uploading,
                    onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                )
            },
        )
        if (loading && mediaList == null) {
            IgngLoadingCenter(loading = true)
        } else if (mediaList != null) {
            val quota = mediaList!!.quota
            val ratio = if (quota.limitBytes > 0L) (quota.usedBytes.toFloat() / quota.limitBytes.toFloat()).coerceIn(0f, 1f) else 0f
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                notice?.let { item { IgngInfoBanner("提示", it, IgngTone.ACCENT) } }
                item {
                    IgngTile(padding = PaddingValues(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("媒体空间配额", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                            Text("${BbsFormatters.formatBytes(quota.usedBytes)} / ${BbsFormatters.formatBytes(quota.limitBytes)}", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                        }
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { ratio }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(IgngRadius.pill)), color = colors.accent, trackColor = colors.line)
                    }
                }
                if (mediaList!!.data.isEmpty()) {
                    item { IgngEmptyTile("还没有上传图片", "点击右上角上传图片，即可一键复制 Markdown 引用链接。") }
                } else {
                    items(mediaList!!.data, key = { it.id }) { file ->
                        IgngTile(padding = PaddingValues(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = container.endpoints.resolveMediaUrl(file.url),
                                    contentDescription = file.originalName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(IgngRadius.control)).background(colors.tileSoft),
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(file.originalName ?: file.id, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary, maxLines = 1)
                                    Text("${BbsFormatters.formatBytes(file.sizeBytes)} · ${file.width}×${file.height}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IgngTagChip(
                                            text = "复制 Markdown",
                                            tone = IgngTone.ACCENT,
                                            onClick = {
                                                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                cm?.setPrimaryClip(ClipData.newPlainText("md", "![](${file.markdownUrl})"))
                                                notice = "已复制图片 Markdown 引用"
                                            },
                                        )
                                        if (file.auditStatus == "rejected" || file.auditStatus == "flagged") {
                                            IgngTagChip(text = "申诉图片", tone = IgngTone.WARNING, onClick = { appealMediaId = file.id })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    appealMediaId?.let { mid ->
        AlertDialog(
            onDismissRequest = { appealMediaId = null },
            title = { Text("提交图片审核申诉") },
            text = {
                OutlinedTextField(value = appealReason, onValueChange = { appealReason = it }, label = { Text("申诉说明") }, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    if (appealReason.isBlank()) return@TextButton
                    appealMediaId = null
                    scope.launch {
                        when (val r = container.creatorRepository.appealMedia(mid, appealReason.trim())) {
                            is ApiResult.Success -> { notice = "图片申诉已提交"; appealReason = ""; load() }
                            is ApiResult.Failure -> notice = r.error.message
                        }
                    }
                }) { Text("提交") }
            },
            dismissButton = { TextButton(onClick = { appealMediaId = null }) { Text("取消") } },
        )
    }
}
