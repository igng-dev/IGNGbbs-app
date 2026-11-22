package net.igng.bbs.feature.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.designsystem.BbsContinueReadingCard
import net.igng.bbs.core.designsystem.BbsPostRowCard
import net.igng.bbs.core.designsystem.BbsRecommendRail
import net.igng.bbs.core.designsystem.BbsSeriesCollapseGroup
import net.igng.bbs.core.designsystem.IgngEmptyTile
import net.igng.bbs.core.designsystem.IgngErrorTile
import net.igng.bbs.core.designsystem.IgngFoldedCoreGlyph
import net.igng.bbs.core.designsystem.IgngLoadingCenter
import net.igng.bbs.core.designsystem.IgngPullToRefreshBox
import net.igng.bbs.core.designsystem.IgngRadius
import net.igng.bbs.core.designsystem.IgngSectionHeader
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.designsystem.igngBentoEnter
import net.igng.bbs.core.designsystem.igngPressScale
import net.igng.bbs.core.model.AppPreferences
import net.igng.bbs.core.model.BbsHomeDto
import net.igng.bbs.core.model.CategoryDto
import net.igng.bbs.core.model.PostSummaryDto
import net.igng.bbs.core.network.ApiResult

private sealed interface FeedDisplayRow {
    data class SinglePost(val post: PostSummaryDto) : FeedDisplayRow
    data class SeriesGroup(val seriesId: String, val seriesName: String, val posts: List<PostSummaryDto>) : FeedDisplayRow
}

@Composable
fun FeedHomeScreen(
    container: BbsAppContainer,
    onOpenPost: (String) -> Unit,
    onOpenCategory: (String, String) -> Unit,
    onOpenTag: (String) -> Unit,
    onOpenUser: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenInbox: () -> Unit,
    onOpenRules: () -> Unit,
    bottomInsetPadding: PaddingValues = PaddingValues(bottom = 96.dp),
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val bootstrap by container.bootstrap.collectAsState()
    val prefs by container.settingsRepository.preferences.collectAsState(initial = AppPreferences())
    val history by container.settingsRepository.readingHistory.collectAsState(initial = emptyList())

    var home by remember { mutableStateOf<BbsHomeDto?>(null) }
    var posts by remember { mutableStateOf<List<PostSummaryDto>>(emptyList()) }
    var sort by remember { mutableStateOf("latest") }
    var selectedCategory by remember { mutableStateOf<CategoryDto?>(null) }
    var page by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val expandedSeries = remember { mutableStateMapOf<String, Boolean>() }

    suspend fun loadInitial(isRefresh: Boolean) {
        if (isRefresh) refreshing = true else if (posts.isEmpty()) loading = true
        errorMessage = null

        if (home == null && !isRefresh) {
            container.feedRepository.loadCachedHome()?.let { cached ->
                home = cached
                if (posts.isEmpty() && selectedCategory == null && sort == "latest") {
                    posts = cached.latestPosts
                }
            }
        }

        val homeRes = if (selectedCategory == null) container.feedRepository.fetchHome() else null
        val postsRes = container.feedRepository.listPosts(
            page = 1,
            pageSize = 20,
            sort = sort,
            category = selectedCategory?.slug,
        )

        if (homeRes is ApiResult.Success) {
            home = homeRes.data
        }
        when (postsRes) {
            is ApiResult.Success -> {
                posts = postsRes.data.data
                page = postsRes.data.pagination.page
                totalPages = postsRes.data.pagination.totalPages.coerceAtLeast(1)
            }
            is ApiResult.Failure -> {
                if (posts.isEmpty()) {
                    errorMessage = postsRes.error.message ?: "无法加载社区动态"
                }
            }
        }
        loading = false
        refreshing = false
    }

    suspend fun loadNextPage() {
        if (loadingMore || page >= totalPages) return
        loadingMore = true
        when (val res = container.feedRepository.listPosts(
            page = page + 1,
            pageSize = 20,
            sort = sort,
            category = selectedCategory?.slug,
        )) {
            is ApiResult.Success -> {
                val seen = posts.mapTo(HashSet()) { it.id }
                posts = posts + res.data.data.filterNot { it.id in seen }
                page = res.data.pagination.page
                totalPages = res.data.pagination.totalPages.coerceAtLeast(1)
            }
            is ApiResult.Failure -> Unit
        }
        loadingMore = false
    }

    LaunchedEffect(sort, selectedCategory?.id) {
        loadInitial(isRefresh = false)
    }

    val displayRows = remember(posts, prefs.collapseSeriesInFeed) {
        if (!prefs.collapseSeriesInFeed) {
            posts.map { FeedDisplayRow.SinglePost(it) }
        } else {
            buildGroupedRows(posts)
        }
    }

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= (listState.layoutInfo.totalItemsCount - 4).coerceAtLeast(0)
        }
    }
    LaunchedEffect(shouldLoadMore, posts.size) {
        if (shouldLoadMore && posts.isNotEmpty() && page < totalPages && !loadingMore) {
            loadNextPage()
        }
    }

    val categories = bootstrap?.categories?.takeIf { it.isNotEmpty() } ?: home?.categories.orEmpty()
    val unreadCount = bootstrap?.viewer?.unread?.total ?: 0
    val continueEntry = history.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding(),
    ) {
        // Brand Header + Search Pill + Inbox Bell
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "IGNG BBS",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = colors.textPrimary,
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(IgngRadius.pill))
                    .background(colors.tile)
                    .border(1.dp, colors.line, RoundedCornerShape(IgngRadius.pill))
                    .igngPressScale(onClick = onOpenSearch)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "搜索",
                    tint = colors.textMuted,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "搜索文章、专栏、作者…",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    maxLines = 1,
                )
            }
            Box {
                IconButton(onClick = onOpenInbox) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsNone,
                        contentDescription = "消息收件箱",
                        tint = colors.textPrimary,
                    )
                }
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(colors.danger),
                    )
                }
            }
        }

        IgngPullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = {
                scope.launch {
                    container.refreshBootstrap()
                    loadInitial(isRefresh = true)
                }
            },
            modifier = Modifier.weight(1f),
        ) {
            if (loading && posts.isEmpty()) {
                IgngLoadingCenter(loading = true)
            } else if (errorMessage != null && posts.isEmpty()) {
                Box(modifier = Modifier.padding(16.dp)) {
                    IgngErrorTile(
                        message = errorMessage!!,
                        onRetry = { scope.launch { loadInitial(isRefresh = false) } },
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 4.dp,
                        bottom = bottomInsetPadding.calculateBottomPadding() + 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Continue Reading Card
                    if (continueEntry != null) {
                        item(key = "continue_reading") {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                BbsContinueReadingCard(
                                    entry = continueEntry,
                                    onResume = { onOpenPost(continueEntry.postId) },
                                )
                            }
                        }
                    }

                    // Recommendations Rail (only on All view)
                    val recs = home?.recommendations.orEmpty()
                    if (selectedCategory == null && recs.isNotEmpty()) {
                        item(key = "rec_header") {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                IgngSectionHeader(
                                    title = "社区精选",
                                    actionLabel = "社区规范",
                                    onAction = onOpenRules,
                                )
                            }
                        }
                        item(key = "rec_rail") {
                            BbsRecommendRail(
                                slots = recs,
                                onPostClick = { onOpenPost(it.id) },
                            )
                        }
                    }

                    // Category Rail
                    if (categories.isNotEmpty()) {
                        item(key = "cat_rail") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                item(key = "cat_all") {
                                    IgngTagChip(
                                        text = "全部",
                                        selected = selectedCategory == null,
                                        onClick = { selectedCategory = null },
                                    )
                                }
                                items(categories, key = { it.id }) { cat ->
                                    IgngTagChip(
                                        text = cat.name,
                                        selected = selectedCategory?.id == cat.id,
                                        onClick = { selectedCategory = cat },
                                    )
                                }
                            }
                        }
                    }

                    // Hot tags horizontal rail on All view
                    val hotTags = home?.hotTags.orEmpty()
                    if (selectedCategory == null && hotTags.isNotEmpty()) {
                        item(key = "hot_tags") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(hotTags.take(12), key = { "tag_${it.name}" }) { tag ->
                                    IgngTagChip(
                                        text = "#${tag.name}",
                                        tone = IgngTone.NEUTRAL,
                                        onClick = { onOpenTag(tag.name) },
                                    )
                                }
                            }
                        }
                    }

                    // Sort Header + Category full view shortcut
                    item(key = "sort_bar") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IgngTagChip(
                                    text = "最新",
                                    selected = sort == "latest",
                                    onClick = { sort = "latest" },
                                )
                                IgngTagChip(
                                    text = "热门",
                                    selected = sort == "hot",
                                    onClick = { sort = "hot" },
                                )
                            }
                            selectedCategory?.let { cat ->
                                Text(
                                    text = "进入「${cat.name}」分区",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.accent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(IgngRadius.pill))
                                        .clickable { onOpenCategory(cat.slug, cat.name) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }
                    }

                    if (displayRows.isEmpty()) {
                        item(key = "empty_feed") {
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                IgngEmptyTile(
                                    title = "这里还没有文章",
                                    subtitle = "换个分类看看，或者去创作中心发布第一篇内容。",
                                )
                            }
                        }
                    } else {
                        itemsIndexed(
                            items = displayRows,
                            key = { _, row ->
                                when (row) {
                                    is FeedDisplayRow.SinglePost -> "post_${row.post.id}"
                                    is FeedDisplayRow.SeriesGroup -> "ser_${row.seriesId}_${row.posts.first().id}"
                                }
                            },
                        ) { index, row ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .igngBentoEnter(index),
                            ) {
                                when (row) {
                                    is FeedDisplayRow.SinglePost -> {
                                        BbsPostRowCard(
                                            post = row.post,
                                            onClick = { onOpenPost(row.post.id) },
                                            onAuthorClick = row.post.author?.id?.let { uid -> { onOpenUser(uid) } },
                                            onTagClick = onOpenTag,
                                        )
                                    }
                                    is FeedDisplayRow.SeriesGroup -> {
                                        val isExp = expandedSeries[row.seriesId] ?: false
                                        BbsSeriesCollapseGroup(
                                            seriesName = row.seriesName,
                                            posts = row.posts,
                                            expanded = isExp,
                                            onToggle = { expandedSeries[row.seriesId] = !isExp },
                                            onPostClick = { onOpenPost(it.id) },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(key = "footer_loader") {
                        androidx.compose.animation.AnimatedVisibility(visible = loadingMore) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                IgngFoldedCoreGlyph(size = 24.dp)
                            }
                        }
                        if (!loadingMore && page >= totalPages && posts.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "已浏览全部内容",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildGroupedRows(posts: List<PostSummaryDto>): List<FeedDisplayRow> {
    val result = mutableListOf<FeedDisplayRow>()
    val emittedSeries = HashSet<String>()
    val bySeries = posts.filter { it.series != null }.groupBy { it.series!!.id }

    for (post in posts) {
        val ser = post.series
        if (ser != null) {
            val siblings = bySeries[ser.id].orEmpty()
            if (siblings.size >= 2) {
                if (emittedSeries.add(ser.id)) {
                    result.add(
                        FeedDisplayRow.SeriesGroup(
                            seriesId = ser.id,
                            seriesName = ser.name,
                            posts = siblings.sortedBy { it.series?.order ?: 0 },
                        ),
                    )
                }
                continue
            }
        }
        result.add(FeedDisplayRow.SinglePost(post))
    }
    return result
}
