package net.igng.bbs.feature.profile

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.data.UpsertBlockRuleRequest
import net.igng.bbs.core.designsystem.BbsContinueReadingCard
import net.igng.bbs.core.designsystem.BbsPostRowCard
import net.igng.bbs.core.designsystem.IgngAvatar
import net.igng.bbs.core.designsystem.IgngEmptyTile
import net.igng.bbs.core.designsystem.IgngErrorTile
import net.igng.bbs.core.designsystem.IgngInfoBanner
import net.igng.bbs.core.designsystem.IgngLoadingCenter
import net.igng.bbs.core.designsystem.IgngPrimaryButton
import net.igng.bbs.core.designsystem.IgngPullToRefreshBox
import net.igng.bbs.core.designsystem.IgngSecondaryButton
import net.igng.bbs.core.designsystem.IgngSectionHeader
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTile
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.designsystem.IgngTopBar
import net.igng.bbs.core.model.BbsFormatters
import net.igng.bbs.core.model.FavoriteItemDto
import net.igng.bbs.core.model.FollowingItemDto
import net.igng.bbs.core.model.UserProfileDto
import net.igng.bbs.core.network.ApiResult

@Composable
fun ProfileCenterScreen(
    container: BbsAppContainer,
    onLogin: () -> Unit,
    onOpenMyProfile: (String) -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenFollowing: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCreator: () -> Unit,
    onOpenAdmin: () -> Unit,
    onOpenAccountSecurity: () -> Unit,
    onOpenPrivacyPreferences: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenRules: () -> Unit,
    bottomInsetPadding: PaddingValues = PaddingValues(bottom = 96.dp),
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val activeAccount by container.sessionStore.activeAccount.collectAsState()
    val bootstrap by container.bootstrap.collectAsState()
    var refreshing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding(),
    ) {
        IgngTopBar(title = "我的")
        IgngPullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = {
                scope.launch {
                    refreshing = true
                    container.refreshBootstrap()
                    refreshing = false
                }
            },
            modifier = Modifier.weight(1f),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = bottomInsetPadding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Identity Bento Hero
                item(key = "identity") {
                    if (activeAccount == null) {
                        IgngTile {
                            Text("登录你的 IGNG 账户", style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "通过官方安全授权登录，解锁点赞收藏、关注互动、多账户切换、云端草稿创作与实时通知。",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textSecondary,
                            )
                            Spacer(Modifier.height(14.dp))
                            IgngPrimaryButton(text = "前往登录", icon = Icons.AutoMirrored.Rounded.Login, onClick = onLogin)
                        }
                    } else {
                        val acc = activeAccount!!
                        val viewer = bootstrap?.viewer
                        IgngTile(onClick = { onOpenMyProfile(acc.publicUserId) }) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                IgngAvatar(avatarUrl = viewer?.user?.avatarUrl ?: acc.avatarUrl, displayName = acc.displayName, size = 56.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(acc.displayName, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colors.textPrimary)
                                    Text("@${acc.username} · 账户编号 #${acc.userId}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        viewer?.level?.let { lvl ->
                                            IgngTagChip("Lv.$lvl · ${viewer.exp ?: 0} EXP", tone = IgngTone.ACCENT)
                                        }
                                        if (viewer?.risk?.article?.active == true || viewer?.risk?.comment?.active == true) {
                                            IgngTagChip("风控观察中", tone = IgngTone.WARNING)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (activeAccount != null) {
                    item(key = "social_shortcuts") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ProfileRowItem("我的收藏", "收藏夹里的精选文章", Icons.Rounded.BookmarkBorder, onOpenFavorites)
                            ProfileRowItem("关注与粉丝", "查看关注列表与互动关系", Icons.Rounded.Group, onOpenFollowing)
                            ProfileRowItem("阅读记录", "本地阅读进度与断点续读", Icons.Rounded.History, onOpenHistory)
                            ProfileRowItem("创作中心", "文章编辑器、专栏与数据趋势", Icons.Rounded.Create, onOpenCreator)
                        }
                    }

                    // Real-time RBAC-gated Pocket Admin Entry (ONLY visible to BBS admins)
                    if (bootstrap?.viewer?.admin?.isBbsAdmin == true) {
                        item(key = "admin_entry") {
                            ProfileRowItem(
                                title = "社区治理控制台",
                                subtitle = "帖子治理 · 人工审核 · 举报处理 · 双域风控",
                                icon = Icons.Rounded.AdminPanelSettings,
                                onClick = onOpenAdmin,
                                accentTint = true,
                            )
                        }
                    }

                    item(key = "account_shortcuts") {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            IgngSectionHeader("账户与隐私")
                            ProfileRowItem("账户与安全", "多账户切换、头像修改与登录设备管理", Icons.Rounded.ManageAccounts, onOpenAccountSecurity)
                            ProfileRowItem("隐私与内容过滤", "个人简介、屏蔽规则与排除偏好", Icons.Rounded.Lock, onOpenPrivacyPreferences)
                        }
                    }
                } else {
                    item(key = "guest_history") {
                        ProfileRowItem("本地阅读记录", "查看近期浏览与阅读进度", Icons.Rounded.History, onOpenHistory)
                    }
                }

                item(key = "app_shortcuts") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        IgngSectionHeader("应用")
                        ProfileRowItem("外观、动效与阅读设置", "主题、三档动效强度、通知与缓存", Icons.Rounded.Settings, onOpenAppSettings)
                        ProfileRowItem("社区规范与申诉准则", "查看现行 IGNG BBS 社区条款", Icons.Rounded.Gavel, onOpenRules)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    accentTint: Boolean = false,
) {
    val colors = IgngTheme.colors
    IgngTile(padding = PaddingValues(14.dp), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = if (accentTint) colors.warning else colors.accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
            }
        }
    }
}

@Composable
fun UserProfileScreen(
    container: BbsAppContainer,
    userIdOrUsername: String,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onRequireLogin: () -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val activeAccount by container.sessionStore.activeAccount.collectAsState()
    var profile by remember { mutableStateOf<UserProfileDto?>(null) }
    var tab by remember { mutableStateOf(0) } // 0=posts, 1=collab, 2=activities
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var notice by remember { mutableStateOf<String?>(null) }

    suspend fun load() {
        loading = true
        error = null
        when (val r = container.profileRepository.getUserProfile(userIdOrUsername)) {
            is ApiResult.Success -> profile = r.data
            is ApiResult.Failure -> error = r.error.message
        }
        loading = false
    }
    LaunchedEffect(userIdOrUsername, activeAccount?.userId) { load() }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(title = profile?.user?.displayName ?: "用户主页", onBack = onBack)
        when {
            loading && profile == null -> IgngLoadingCenter(loading = true)
            error != null && profile == null -> Box(Modifier.padding(16.dp)) {
                IgngErrorTile(message = error!!, onRetry = { scope.launch { load() } })
            }
            profile != null -> {
                val p = profile!!
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    notice?.let { item { IgngInfoBanner("提示", it, IgngTone.ACCENT) } }
                    item {
                        IgngTile {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                IgngAvatar(p.user.avatarUrl, p.user.displayName, size = 56.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(p.user.displayName, style = MaterialTheme.typography.titleLarge, color = colors.textPrimary)
                                    Text("@${p.user.username} · Lv.${p.level}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                }
                            }
                            p.user.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                                Spacer(Modifier.height(8.dp))
                                Text(bio, style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("${p.stats.posts} 文章", style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
                                Text("${p.stats.followers} 粉丝", style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
                                Text("${p.stats.following} 关注", style = MaterialTheme.typography.labelMedium, color = colors.textPrimary)
                                Text("${BbsFormatters.compactCount(p.stats.totalLikes)} 获赞", style = MaterialTheme.typography.labelMedium, color = colors.textMuted)
                            }
                            if (!p.viewerState.isSelf) {
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IgngPrimaryButton(
                                        text = if (p.viewerState.isFollowing) "已关注" else "关注",
                                        onClick = {
                                            if (activeAccount == null) { onRequireLogin(); return@IgngPrimaryButton }
                                            scope.launch {
                                                when (val r = container.profileRepository.toggleFollow(p.user.id)) {
                                                    is ApiResult.Success -> load()
                                                    is ApiResult.Failure -> notice = r.error.message
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                    IgngSecondaryButton(
                                        text = if (p.viewerState.blockFollow || p.viewerState.blockView) "取消屏蔽" else "屏蔽",
                                        onClick = {
                                            if (activeAccount == null) { onRequireLogin(); return@IgngSecondaryButton }
                                            scope.launch {
                                                val res = if (p.viewerState.blockFollow || p.viewerState.blockView) {
                                                    container.profileRepository.deleteBlockRule(p.user.id)
                                                } else {
                                                    container.profileRepository.upsertBlockRule(UpsertBlockRuleRequest(p.user.id, blockFollow = true, blockView = true))
                                                }
                                                when (res) {
                                                    is ApiResult.Success -> { notice = "屏蔽设置已更新"; load() }
                                                    is ApiResult.Failure -> notice = res.error.message
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }

                    // Pure BBS 3-tab strip (zero Ink)
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IgngTagChip("已发布（${p.posts.size}）", selected = tab == 0, onClick = { tab = 0 })
                            IgngTagChip("参与协作（${p.collaboratedPosts.size}）", selected = tab == 1, onClick = { tab = 1 })
                            IgngTagChip("近7天动态（${p.recentActivities.size}）", selected = tab == 2, onClick = { tab = 2 })
                        }
                    }

                    when (tab) {
                        0 -> if (p.posts.isEmpty()) {
                            item { IgngEmptyTile("暂无公开文章", "这位作者还没有发布公开的 BBS 文章。") }
                        } else {
                            items(p.posts, key = { it.id }) { post ->
                                BbsPostRowCard(post = post, onClick = { onOpenPost(post.id) })
                            }
                        }
                        1 -> if (p.collaboratedPosts.isEmpty()) {
                            item { IgngEmptyTile("暂无协作文章", "暂无公开的协作署名记录。") }
                        } else {
                            items(p.collaboratedPosts, key = { "${it.post.id}_${it.roleTitle}" }) { item ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IgngTagChip("署名角色：${item.roleTitle}", tone = IgngTone.ACCENT)
                                    BbsPostRowCard(post = item.post, onClick = { onOpenPost(item.post.id) })
                                }
                            }
                        }
                        else -> if (p.recentActivities.isEmpty()) {
                            item { IgngEmptyTile("近 7 天暂无动态", "近期没有新的发布、评论或点赞互动。") }
                        } else {
                            items(p.recentActivities, key = { "${it.kind}_${it.targetId}_${it.createdAt}" }) { act ->
                                IgngTile(padding = PaddingValues(14.dp), onClick = act.targetId?.let { pid -> { onOpenPost(pid) } }) {
                                    Text(
                                        text = when (act.kind) {
                                            "post" -> "发布了文章《${act.title}》"
                                            "comment" -> "评论了《${act.title}》"
                                            else -> "赞了文章《${act.title}》"
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = colors.textPrimary,
                                    )
                                    act.excerpt?.takeIf { it.isNotBlank() }?.let {
                                        Spacer(Modifier.height(4.dp))
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 2)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(BbsFormatters.relativeTime(act.createdAt), style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
) {
    val colors = IgngTheme.colors
    var items by remember { mutableStateOf<List<FavoriteItemDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    suspend fun load() {
        loading = true; error = null
        when (val r = container.profileRepository.listFavorites(pageSize = 40)) {
            is ApiResult.Success -> items = r.data.data
            is ApiResult.Failure -> error = r.error.message
        }
        loading = false
    }
    LaunchedEffect(Unit) { load() }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(title = "我的收藏", onBack = onBack)
        when {
            loading -> IgngLoadingCenter(loading = true)
            error != null -> Box(Modifier.padding(16.dp)) { IgngErrorTile(error!!, onRetry = { scope.launch { load() } }) }
            items.isEmpty() -> Box(Modifier.padding(16.dp)) { IgngEmptyTile("收藏夹还是空的", "在阅读文章时点击底部书签图标即可收藏。") }
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items, key = { it.post.id }) { fav ->
                    BbsPostRowCard(post = fav.post, onClick = { onOpenPost(fav.post.id) })
                }
            }
        }
    }
}

@Composable
fun FollowingScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
    onOpenUser: (String) -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    var tab by remember { mutableStateOf("following") }
    var list by remember { mutableStateOf<List<FollowingItemDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    suspend fun load() {
        loading = true
        when (val r = container.profileRepository.listFollowing(tab = tab, pageSize = 50)) {
            is ApiResult.Success -> list = r.data.data
            is ApiResult.Failure -> Unit
        }
        loading = false
    }
    LaunchedEffect(tab) { load() }

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(
            title = "关注与粉丝",
            onBack = onBack,
            actions = {
                IgngTagChip("我关注的", selected = tab == "following", onClick = { tab = "following" })
                Spacer(Modifier.width(4.dp))
                IgngTagChip("关注我的", selected = tab == "followers", onClick = { tab = "followers" })
            },
        )
        if (loading) {
            IgngLoadingCenter(loading = true)
        } else if (list.isEmpty()) {
            Box(Modifier.padding(16.dp)) { IgngEmptyTile("列表为空", "暂时还没有相关用户记录。") }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(list, key = { it.user.id }) { row ->
                    IgngTile(padding = PaddingValues(14.dp), onClick = { onOpenUser(row.user.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IgngAvatar(row.user.avatarUrl, row.user.displayName, size = 40.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(row.user.displayName, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                Text("@${row.user.username}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                            }
                            IgngTagChip(
                                text = if (row.isFollowing) "已关注" else "+ 回关",
                                selected = row.isFollowing,
                                tone = IgngTone.ACCENT,
                                onClick = {
                                    scope.launch {
                                        container.profileRepository.toggleFollow(row.user.id)
                                        load()
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadingHistoryScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val history by container.settingsRepository.readingHistory.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize().background(colors.canvas).statusBarsPadding()) {
        IgngTopBar(
            title = "本地阅读记录",
            onBack = onBack,
            actions = {
                if (history.isNotEmpty()) {
                    IgngTagChip("清空记录", tone = IgngTone.DANGER, onClick = { scope.launch { container.settingsRepository.clearReadingHistory() } })
                }
            },
        )
        if (history.isEmpty()) {
            Box(Modifier.padding(16.dp)) { IgngEmptyTile("暂无阅读记录", "你浏览过的文章进度会自动保存在本机，方便下次继续阅读。") }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history, key = { it.postId }) { entry ->
                    BbsContinueReadingCard(entry = entry, onResume = { onOpenPost(entry.postId) })
                }
            }
        }
    }
}
