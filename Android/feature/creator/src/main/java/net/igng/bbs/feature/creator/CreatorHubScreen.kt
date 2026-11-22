package net.igng.bbs.feature.creator

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.FolderShared
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.PermMedia
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.designsystem.BbsPostRowCard
import net.igng.bbs.core.designsystem.BbsRiskMeterCard
import net.igng.bbs.core.designsystem.ChartPoint
import net.igng.bbs.core.designsystem.IgngEmptyTile
import net.igng.bbs.core.designsystem.IgngErrorTile
import net.igng.bbs.core.designsystem.IgngLineChart
import net.igng.bbs.core.designsystem.IgngLoadingCenter
import net.igng.bbs.core.designsystem.IgngPrimaryButton
import net.igng.bbs.core.designsystem.IgngPullToRefreshBox
import net.igng.bbs.core.designsystem.IgngRadius
import net.igng.bbs.core.designsystem.IgngSectionHeader
import net.igng.bbs.core.designsystem.IgngStatTile
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTile
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.designsystem.IgngTopBar
import net.igng.bbs.core.model.BbsFormatters
import net.igng.bbs.core.model.CreatorOverviewDto
import net.igng.bbs.core.network.ApiResult

@Composable
fun CreatorHubScreen(
    container: BbsAppContainer,
    onNewArticle: () -> Unit,
    onEditPost: (String) -> Unit,
    onOpenPosts: () -> Unit,
    onOpenColumns: () -> Unit,
    onOpenSeries: () -> Unit,
    onOpenComments: () -> Unit,
    onOpenReviews: () -> Unit,
    onOpenMedia: () -> Unit,
    onRequireLogin: () -> Unit,
    bottomInsetPadding: PaddingValues = PaddingValues(bottom = 96.dp),
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val activeAccount by container.sessionStore.activeAccount.collectAsState()

    var overview by remember { mutableStateOf<CreatorOverviewDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun load(isRefresh: Boolean) {
        if (activeAccount == null) {
            loading = false
            return
        }
        if (isRefresh) refreshing = true else if (overview == null) loading = true
        error = null
        when (val res = container.creatorRepository.getOverview()) {
            is ApiResult.Success -> overview = res.data
            is ApiResult.Failure -> if (overview == null) error = res.error.message
        }
        loading = false
        refreshing = false
    }

    LaunchedEffect(activeAccount?.userId) { load(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding(),
    ) {
        IgngTopBar(
            title = "创作中心",
            actions = {
                if (activeAccount != null) {
                    IgngPrimaryButton(
                        text = "写文章",
                        icon = Icons.Rounded.Add,
                        onClick = onNewArticle,
                    )
                }
            },
        )

        if (activeAccount == null) {
            Box(Modifier.padding(16.dp)) {
                IgngEmptyTile(
                    title = "登录开启 BBS 创作空间",
                    subtitle = "使用原生所见即所得块级编辑器撰写长文、管理专栏与系列合集、查看 30 天阅读趋势与审核申诉状态。",
                    actionLabel = "登录 IGNG 账户",
                    onAction = onRequireLogin,
                )
            }
            return
        }

        IgngPullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { scope.launch { load(true) } },
            modifier = Modifier.weight(1f),
        ) {
            when {
                loading && overview == null -> IgngLoadingCenter(loading = true)
                error != null && overview == null -> Box(Modifier.padding(16.dp)) {
                    IgngErrorTile(message = error!!, onRetry = { scope.launch { load(false) } })
                }
                overview != null -> {
                    val data = overview!!
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 4.dp,
                            bottom = bottomInsetPadding.calculateBottomPadding() + 16.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Bento Totals 2x2 Grid
                        item(key = "stats_row_1") {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                IgngStatTile(
                                    label = "已发布文章",
                                    value = data.totals.published.toString(),
                                    hint = "草稿 ${data.totals.drafts} · 审核中 ${data.totals.pending}",
                                    tone = IgngTone.ACCENT,
                                    modifier = Modifier.weight(1f),
                                )
                                IgngStatTile(
                                    label = "累计阅读",
                                    value = BbsFormatters.compactCount(data.totals.views),
                                    hint = "获赞 ${BbsFormatters.compactCount(data.totals.likes)} · 评论 ${BbsFormatters.compactCount(data.totals.comments)}",
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        // Daily Submission Budget
                        item(key = "daily_budget") {
                            val budget = data.dailyBudget
                            val ratio = if (budget.limit > 0) (budget.used.toFloat() / budget.limit.toFloat()).coerceIn(0f, 1f) else 0f
                            IgngTile(padding = PaddingValues(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text("今日投稿额度（Lv.${budget.level}）", style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                    Text(
                                        text = "${budget.used} / ${budget.limit}",
                                        style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                                        color = if (budget.allowed) colors.accent else colors.danger,
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(IgngRadius.pill)),
                                    color = if (budget.allowed) colors.accent else colors.danger,
                                    trackColor = colors.line,
                                )
                            }
                        }

                        // Pending Todos Banner
                        val todos = data.todos
                        val totalTodos = todos.pendingPostInvites + todos.pendingColumnEvents + todos.pendingSubscriptions + todos.rejectedPosts
                        if (totalTodos > 0) {
                            item(key = "todos") {
                                IgngTile(soft = true) {
                                    Text("待处理事项（$totalTodos）", style = MaterialTheme.typography.titleSmall, color = colors.warning)
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (todos.pendingPostInvites > 0) {
                                            IgngTagChip(text = "协作邀请 ${todos.pendingPostInvites}", tone = IgngTone.ACCENT, onClick = onOpenPosts)
                                        }
                                        if (todos.pendingColumnEvents + todos.pendingSubscriptions > 0) {
                                            IgngTagChip(text = "专栏待审 ${todos.pendingColumnEvents + todos.pendingSubscriptions}", tone = IgngTone.WARNING, onClick = onOpenColumns)
                                        }
                                        if (todos.rejectedPosts > 0) {
                                            IgngTagChip(text = "未通过 ${todos.rejectedPosts}", tone = IgngTone.DANGER, onClick = onOpenReviews)
                                        }
                                    }
                                }
                            }
                        }

                        // 30-day Views Trend
                        item(key = "views_chart") {
                            IgngTile {
                                Text("近 30 天阅读趋势", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Spacer(Modifier.height(10.dp))
                                IgngLineChart(
                                    points = data.viewsTrend.map { ChartPoint(it.date, it.views) },
                                )
                            }
                        }

                        // Quick Management Bento Grid
                        item(key = "quick_grid") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                IgngSectionHeader(title = "创作管理工具")
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    CreatorShortcutTile("文章与草稿", "版本 · 协作 · 数据", Icons.AutoMirrored.Rounded.Article, onOpenPosts, Modifier.weight(1f))
                                    CreatorShortcutTile("专栏管理", "成员 · 订阅 · 置顶", Icons.Rounded.FolderShared, onOpenColumns, Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    CreatorShortcutTile("系列合集", "章节编排 · 趋势", Icons.Rounded.AutoStories, onOpenSeries, Modifier.weight(1f))
                                    CreatorShortcutTile("评论管理", "收到的文章讨论", Icons.AutoMirrored.Rounded.Comment, onOpenComments, Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    CreatorShortcutTile("审核与申诉", "进度 · 复核申诉", Icons.Rounded.Gavel, onOpenReviews, Modifier.weight(1f))
                                    CreatorShortcutTile("媒体资产", "配额 · 图床上传", Icons.Rounded.PermMedia, onOpenMedia, Modifier.weight(1f))
                                }
                            }
                        }

                        // Dual-domain Risk Meter
                        item(key = "risk_card") {
                            BbsRiskMeterCard(risk = data.risk)
                        }

                        // Continue Writing
                        if (data.continueWriting.isNotEmpty()) {
                            item(key = "continue_header") {
                                IgngSectionHeader(title = "最近编辑", actionLabel = "全部文章", onAction = onOpenPosts)
                            }
                            items(data.continueWriting, key = { "cw_${it.id}" }) { post ->
                                BbsPostRowCard(
                                    post = post,
                                    onClick = { onEditPost(post.id) },
                                    showModerationBadge = true,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreatorShortcutTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    IgngTile(modifier = modifier, padding = PaddingValues(14.dp), onClick = onClick) {
        Icon(icon, contentDescription = null, tint = colors.accent)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
        Spacer(Modifier.height(2.dp))
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
    }
}
