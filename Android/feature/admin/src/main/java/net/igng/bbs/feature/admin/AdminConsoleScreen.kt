package net.igng.bbs.feature.admin

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.igng.bbs.core.data.AdminDecideReportRequest
import net.igng.bbs.core.data.AdminDecideReviewRequest
import net.igng.bbs.core.data.AdminManageRiskRequest
import net.igng.bbs.core.data.AdminUpdateColumnRequest
import net.igng.bbs.core.data.AdminUpdatePostRequest
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.designsystem.IgngEmptyTile
import net.igng.bbs.core.designsystem.IgngInfoBanner
import net.igng.bbs.core.designsystem.IgngLoadingCenter
import net.igng.bbs.core.designsystem.IgngPrimaryButton
import net.igng.bbs.core.designsystem.IgngPullToRefreshBox
import net.igng.bbs.core.designsystem.IgngSectionHeader
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTile
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.designsystem.IgngTopBar
import net.igng.bbs.core.model.AdminReviewItemDto
import net.igng.bbs.core.model.AdminRiskEntryDto
import net.igng.bbs.core.model.AdminUserRowDto
import net.igng.bbs.core.model.BbsFormatters
import net.igng.bbs.core.model.ColumnSummaryDto
import net.igng.bbs.core.model.PostSummaryDto
import net.igng.bbs.core.model.SeriesSummaryDto
import net.igng.bbs.core.network.ApiResult

private enum class AdminTab(val label: String) {
    POSTS("帖子治理"),
    REVIEWS("人工审核"),
    REPORTS("举报队列"),
    RISK("双域风控"),
    USERS("用户检索"),
    COLLECTIONS("专栏与系列"),
}

@Composable
fun AdminConsoleScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenUser: (String) -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val bootstrap by container.bootstrap.collectAsState()
    val adminAccess = bootstrap?.viewer?.admin

    var selectedTab by remember { mutableStateOf(AdminTab.REVIEWS) }
    var search by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }

    var posts by remember { mutableStateOf<List<PostSummaryDto>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<AdminReviewItemDto>>(emptyList()) }
    var reports by remember { mutableStateOf<List<AdminReviewItemDto>>(emptyList()) }
    var riskEntries by remember { mutableStateOf<List<AdminRiskEntryDto>>(emptyList()) }
    var users by remember { mutableStateOf<List<AdminUserRowDto>>(emptyList()) }
    var columns by remember { mutableStateOf<List<ColumnSummaryDto>>(emptyList()) }
    var series by remember { mutableStateOf<List<SeriesSummaryDto>>(emptyList()) }

    // Manual risk dialog
    var showRiskDialog by remember { mutableStateOf(false) }
    var riskUserId by remember { mutableStateOf("") }
    var riskScope by remember { mutableStateOf("article") }
    var riskDays by remember { mutableStateOf("30") }
    var riskReason by remember { mutableStateOf("") }

    // Reject/Uphold reason dialog
    var decideDialogTarget by remember { mutableStateOf<Triple<String, Boolean, Boolean>?>(null) } // (id, isReport, approveOrDismiss)
    var decideReason by remember { mutableStateOf("") }

    suspend fun loadCurrent(isRefresh: Boolean) {
        if (adminAccess?.isBbsAdmin != true) {
            loading = false
            return
        }
        if (isRefresh) refreshing = true else loading = true
        val q = search.trim().ifBlank { null }
        when (selectedTab) {
            AdminTab.POSTS -> when (val r = container.adminRepository.listPosts(query = q)) {
                is ApiResult.Success -> posts = r.data.data
                is ApiResult.Failure -> notice = r.error.message
            }
            AdminTab.REVIEWS -> when (val r = container.adminRepository.listReviews()) {
                is ApiResult.Success -> reviews = r.data.data
                is ApiResult.Failure -> notice = r.error.message
            }
            AdminTab.REPORTS -> when (val r = container.adminRepository.listReports()) {
                is ApiResult.Success -> reports = r.data.data
                is ApiResult.Failure -> notice = r.error.message
            }
            AdminTab.RISK -> when (val r = container.adminRepository.listRiskStates()) {
                is ApiResult.Success -> riskEntries = r.data.data
                is ApiResult.Failure -> notice = r.error.message
            }
            AdminTab.USERS -> when (val r = container.adminRepository.listUsers(query = q)) {
                is ApiResult.Success -> users = r.data.data
                is ApiResult.Failure -> notice = r.error.message
            }
            AdminTab.COLLECTIONS -> {
                when (val r = container.adminRepository.listColumns(query = q)) {
                    is ApiResult.Success -> columns = r.data.data
                    is ApiResult.Failure -> notice = r.error.message
                }
                when (val s = container.adminRepository.listSeries(query = q)) {
                    is ApiResult.Success -> series = s.data.data
                    is ApiResult.Failure -> Unit
                }
            }
        }
        loading = false
        refreshing = false
    }

    LaunchedEffect(selectedTab, adminAccess?.isBbsAdmin) { loadCurrent(false) }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(
            title = "BBS 社区治理控制台",
            subtitle = adminAccess?.groups?.takeIf { it.isNotEmpty() }?.joinToString(" · "),
            onBack = onBack,
        )

        if (adminAccess?.isBbsAdmin != true) {
            Box(Modifier.padding(16.dp)) {
                IgngEmptyTile(
                    title = "当前账户无 BBS 管理权限",
                    subtitle = "治理控制台实时校验服务端 RBAC 权限组，只有具备 BBS 管理员或超级管理员权限的账户可访问。",
                )
            }
            return
        }

        val visibleTabs = AdminTab.entries.filter { it != AdminTab.USERS || adminAccess.canManageUsers }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(visibleTabs, key = { it.name }) { tab ->
                IgngTagChip(text = tab.label, selected = selectedTab == tab, onClick = { selectedTab = tab })
            }
        }
        Spacer(Modifier.height(8.dp))

        if (selectedTab in listOf(AdminTab.POSTS, AdminTab.USERS, AdminTab.COLLECTIONS)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text("搜索关键词或编号", style = MaterialTheme.typography.bodySmall) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                IgngTagChip("查询", tone = IgngTone.ACCENT, onClick = { scope.launch { loadCurrent(true) } })
            }
            Spacer(Modifier.height(8.dp))
        }

        IgngPullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { scope.launch { loadCurrent(true) } },
            modifier = Modifier.weight(1f),
        ) {
            if (loading) {
                IgngLoadingCenter(loading = true)
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    notice?.let { item { IgngInfoBanner("操作结果", it, IgngTone.ACCENT) } }

                    when (selectedTab) {
                        AdminTab.POSTS -> {
                            if (posts.isEmpty()) item { IgngEmptyTile("无匹配文章", "调整筛选关键词再试。") }
                            items(posts, key = { it.id }) { p ->
                                IgngTile {
                                    Text("${p.title}（${p.id}）", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                    Text("作者 ${p.author?.displayName ?: "—"} · 状态：${BbsFormatters.postStatusLabel(p.status, p.banStatus, p.moderationStatus)}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                    Spacer(Modifier.height(8.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        item { IgngTagChip("查看", onClick = { onOpenPost(p.id) }) }
                                        item { IgngTagChip("恢复正常", tone = IgngTone.SUCCESS, onClick = { scope.launch { container.adminRepository.updatePost(p.id, AdminUpdatePostRequest(banStatus = 0)); notice = "已恢复正常"; loadCurrent(true) } }) }
                                        item { IgngTagChip("仅作者可见", tone = IgngTone.WARNING, onClick = { scope.launch { container.adminRepository.updatePost(p.id, AdminUpdatePostRequest(banStatus = 1)); notice = "已设为仅作者可见"; loadCurrent(true) } }) }
                                        item { IgngTagChip("受限提示", tone = IgngTone.WARNING, onClick = { scope.launch { container.adminRepository.updatePost(p.id, AdminUpdatePostRequest(banStatus = 4)); notice = "已设为受限提示"; loadCurrent(true) } }) }
                                        item { IgngTagChip("禁止查看", tone = IgngTone.DANGER, onClick = { scope.launch { container.adminRepository.updatePost(p.id, AdminUpdatePostRequest(banStatus = 2)); notice = "已禁止查看"; loadCurrent(true) } }) }
                                        item { IgngTagChip("软删除", tone = IgngTone.DANGER, onClick = { scope.launch { container.adminRepository.deletePost(p.id); notice = "文章已软删除"; loadCurrent(true) } }) }
                                    }
                                }
                            }
                        }

                        AdminTab.REVIEWS -> {
                            if (reviews.isEmpty()) item { IgngEmptyTile("审核队列已清空", "当前没有待人工复核的任务。") }
                            items(reviews, key = { it.id }) { rev ->
                                IgngTile {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${rev.targetType} · ${rev.targetId} (${rev.reviewType})", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                        IgngTagChip("优先级 ${rev.priority}", tone = IgngTone.WARNING)
                                    }
                                    rev.titleSnapshot?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary) }
                                    rev.contentSnapshot?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 4) }
                                    rev.reason?.let { Text("备注：$it", style = MaterialTheme.typography.labelSmall, color = colors.textMuted) }
                                    Spacer(Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IgngTagChip("通过", tone = IgngTone.SUCCESS, onClick = {
                                            scope.launch {
                                                when (val r = container.adminRepository.decideReview(rev.id, AdminDecideReviewRequest("approve"))) {
                                                    is ApiResult.Success -> { notice = "已批准 ${rev.id}"; loadCurrent(true) }
                                                    is ApiResult.Failure -> notice = r.error.message
                                                }
                                            }
                                        })
                                        IgngTagChip("驳回", tone = IgngTone.DANGER, onClick = {
                                            decideDialogTarget = Triple(rev.id, false, false)
                                            decideReason = ""
                                        })
                                    }
                                }
                            }
                        }

                        AdminTab.REPORTS -> {
                            if (reports.isEmpty()) item { IgngEmptyTile("举报队列为空", "当前没有待处理的社区举报。") }
                            items(reports, key = { it.id }) { rep ->
                                IgngTile {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${rep.targetType} · ${rep.targetId}", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                        IgngTagChip(rep.reasonCode ?: "举报", tone = IgngTone.DANGER)
                                    }
                                    rep.reason?.let { Text("举报说明：$it", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary) }
                                    rep.contentSnapshot?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.textMuted, maxLines = 3) }
                                    Spacer(Modifier.height(10.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IgngTagChip("举报成立（处置）", tone = IgngTone.DANGER, onClick = {
                                            decideDialogTarget = Triple(rep.id, true, false)
                                            decideReason = ""
                                        })
                                        IgngTagChip("驳回举报（正常）", tone = IgngTone.SUCCESS, onClick = {
                                            scope.launch {
                                                when (val r = container.adminRepository.decideReport(rep.id, AdminDecideReportRequest("dismiss"))) {
                                                    is ApiResult.Success -> { notice = "已驳回该举报"; loadCurrent(true) }
                                                    is ApiResult.Failure -> notice = r.error.message
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        }

                        AdminTab.RISK -> {
                            item {
                                IgngPrimaryButton(text = "手动设置用户风控", onClick = { showRiskDialog = true })
                            }
                            if (riskEntries.isEmpty()) item { IgngEmptyTile("当前无活跃风控记录", "全站用户均处于正常审核通道。") }
                            items(riskEntries, key = { "${it.user.id}_${it.scope}" }) { entry ->
                                IgngTile {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${entry.user.displayName} (${entry.user.id})", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                            Text("域：${entry.scope} · 触发：${entry.triggerKind} · 截止 ${BbsFormatters.fullDateTime(entry.activeUntil)}", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                            entry.reason?.let { Text("原因：$it", style = MaterialTheme.typography.labelSmall, color = colors.textMuted) }
                                        }
                                        IgngTagChip("解除", tone = IgngTone.SUCCESS, onClick = {
                                            scope.launch {
                                                when (val r = container.adminRepository.manageRisk(AdminManageRiskRequest(entry.user.id, entry.scope, "clear"))) {
                                                    is ApiResult.Success -> { notice = "已解除 ${entry.user.displayName} 的 ${entry.scope} 风控"; loadCurrent(true) }
                                                    is ApiResult.Failure -> notice = r.error.message
                                                }
                                            }
                                        })
                                    }
                                }
                            }
                        }

                        AdminTab.USERS -> {
                            if (users.isEmpty()) item { IgngEmptyTile("未检索到用户", "输入用户名、昵称或账户编号进行查询。") }
                            items(users, key = { it.id }) { u ->
                                IgngTile(onClick = { onOpenUser(u.id) }) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("${u.nickname.ifBlank { u.username }} (@${u.username})", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                        IgngTagChip(u.status, tone = if (u.status == "normal") IgngTone.SUCCESS else IgngTone.DANGER)
                                    }
                                    Text("${u.id} · ${u.postCount} 篇文章 · ${u.commentCount} 条评论", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                    Spacer(Modifier.height(6.dp))
                                    IgngTagChip("设置风控", tone = IgngTone.WARNING, onClick = { riskUserId = u.id; showRiskDialog = true })
                                }
                            }
                        }

                        AdminTab.COLLECTIONS -> {
                            item { IgngSectionHeader("专栏列表（${columns.size}）") }
                            items(columns, key = { it.id }) { c ->
                                IgngTile {
                                    Text("${c.name} (/${c.slug})", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                    Text("${c.postCount} 篇 · ${c.memberCount} 成员 · join=${c.joinMode}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        IgngTagChip("恢复简介状态", onClick = {
                                            scope.launch { container.adminRepository.updateColumn(c.id, AdminUpdateColumnRequest(bioStatus = 0)); notice = "已更新专栏状态"; loadCurrent(true) }
                                        })
                                        IgngTagChip("设为仅受邀", tone = IgngTone.WARNING, onClick = {
                                            scope.launch { container.adminRepository.updateColumn(c.id, AdminUpdateColumnRequest(joinMode = "invite_only")); notice = "已设为仅受邀"; loadCurrent(true) }
                                        })
                                    }
                                }
                            }
                            item { IgngSectionHeader("系列列表（${series.size}）") }
                            items(series, key = { it.id }) { s ->
                                IgngTile {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(s.name, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                            Text("${s.postCount} 篇 · 作者 ${s.creator?.displayName ?: "—"}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                        }
                                        IgngTagChip("删除", tone = IgngTone.DANGER, onClick = {
                                            scope.launch { container.adminRepository.deleteSeries(s.id); notice = "已删除系列"; loadCurrent(true) }
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

    decideDialogTarget?.let { (id, isReport, _) ->
        AlertDialog(
            onDismissRequest = { decideDialogTarget = null },
            title = { Text(if (isReport) "确认举报成立并处置" else "驳回该审核任务") },
            text = { OutlinedTextField(value = decideReason, onValueChange = { decideReason = it }, label = { Text("处置原因（将反馈给作者）") }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                TextButton(onClick = {
                    decideDialogTarget = null
                    scope.launch {
                        val r = if (isReport) {
                            container.adminRepository.decideReport(id, AdminDecideReportRequest("uphold", reason = decideReason.trim().ifBlank { null }))
                        } else {
                            container.adminRepository.decideReview(id, AdminDecideReviewRequest("reject", reason = decideReason.trim().ifBlank { null }))
                        }
                        when (r) {
                            is ApiResult.Success -> { notice = "处置已完成"; loadCurrent(true) }
                            is ApiResult.Failure -> notice = r.error.message
                        }
                    }
                }) { Text("确认处置") }
            },
            dismissButton = { TextButton(onClick = { decideDialogTarget = null }) { Text("取消") } },
        )
    }

    if (showRiskDialog) {
        AlertDialog(
            onDismissRequest = { showRiskDialog = false },
            title = { Text("手动设置用户风控") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = riskUserId, onValueChange = { riskUserId = it }, label = { Text("目标账户编号（如 usr_12）") })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IgngTagChip("文章域", selected = riskScope == "article", onClick = { riskScope = "article" })
                        IgngTagChip("评论域", selected = riskScope == "comment", onClick = { riskScope = "comment" })
                    }
                    OutlinedTextField(value = riskDays, onValueChange = { riskDays = it }, label = { Text("持续天数（1–365）") })
                    OutlinedTextField(value = riskReason, onValueChange = { riskReason = it }, label = { Text("原因说明") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (riskUserId.isBlank()) return@TextButton
                    showRiskDialog = false
                    scope.launch {
                        when (val r = container.adminRepository.manageRisk(
                            AdminManageRiskRequest(
                                userId = riskUserId.trim(),
                                scope = riskScope,
                                action = "set",
                                days = riskDays.toIntOrNull()?.coerceIn(1, 365) ?: 30,
                                reason = riskReason.trim().ifBlank { null },
                            ),
                        )) {
                            is ApiResult.Success -> { notice = "已设置 ${riskUserId} 的 $riskScope 风控"; riskUserId = ""; riskReason = ""; loadCurrent(true) }
                            is ApiResult.Failure -> notice = r.error.message
                        }
                    }
                }) { Text("确认设置") }
            },
            dismissButton = { TextButton(onClick = { showRiskDialog = false }) { Text("取消") } },
        )
    }
}
