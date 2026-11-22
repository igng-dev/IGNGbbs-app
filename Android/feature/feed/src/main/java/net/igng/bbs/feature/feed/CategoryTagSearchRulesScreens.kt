package net.igng.bbs.feature.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.designsystem.BbsPostRowCard
import net.igng.bbs.core.designsystem.IgngAvatar
import net.igng.bbs.core.designsystem.IgngEmptyTile
import net.igng.bbs.core.designsystem.IgngErrorTile
import net.igng.bbs.core.designsystem.IgngLoadingCenter
import net.igng.bbs.core.designsystem.IgngPullToRefreshBox
import net.igng.bbs.core.designsystem.IgngRadius
import net.igng.bbs.core.designsystem.IgngSectionHeader
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTile
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.designsystem.IgngTopBar
import net.igng.bbs.core.designsystem.igngBentoEnter
import net.igng.bbs.core.model.BbsRulesDto
import net.igng.bbs.core.model.PostSummaryDto
import net.igng.bbs.core.model.SearchResponseDto
import net.igng.bbs.core.model.TagStatDto
import net.igng.bbs.core.model.V1Id
import net.igng.bbs.core.network.ApiResult

@Composable
fun CategoryFeedScreen(
    container: BbsAppContainer,
    categorySlug: String,
    categoryTitle: String,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenUser: (String) -> Unit,
    onOpenTag: (String) -> Unit,
) {
    FilteredPostListScreen(
        container = container,
        title = categoryTitle.ifBlank { categorySlug },
        subtitle = "分区文章",
        categorySlug = categorySlug,
        tag = null,
        onBack = onBack,
        onOpenPost = onOpenPost,
        onOpenUser = onOpenUser,
        onOpenTag = onOpenTag,
    )
}

@Composable
fun TagFeedScreen(
    container: BbsAppContainer,
    tag: String,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenUser: (String) -> Unit,
    onOpenTag: (String) -> Unit,
) {
    FilteredPostListScreen(
        container = container,
        title = "#$tag",
        subtitle = "标签聚合",
        categorySlug = null,
        tag = tag,
        onBack = onBack,
        onOpenPost = onOpenPost,
        onOpenUser = onOpenUser,
        onOpenTag = onOpenTag,
    )
}

@Composable
private fun FilteredPostListScreen(
    container: BbsAppContainer,
    title: String,
    subtitle: String,
    categorySlug: String?,
    tag: String?,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenUser: (String) -> Unit,
    onOpenTag: (String) -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    var sort by remember { mutableStateOf("latest") }
    var posts by remember { mutableStateOf<List<PostSummaryDto>>(emptyList()) }
    var page by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    suspend fun load(reset: Boolean) {
        if (reset) {
            if (posts.isEmpty()) loading = true else refreshing = true
        }
        error = null
        val targetPage = if (reset) 1 else page + 1
        when (val res = container.feedRepository.listPosts(
            page = targetPage,
            pageSize = 20,
            sort = sort,
            category = categorySlug,
            tag = tag,
        )) {
            is ApiResult.Success -> {
                posts = if (reset) res.data.data else posts + res.data.data
                page = res.data.pagination.page
                totalPages = res.data.pagination.totalPages.coerceAtLeast(1)
            }
            is ApiResult.Failure -> {
                if (posts.isEmpty()) error = res.error.message
            }
        }
        loading = false
        refreshing = false
    }

    LaunchedEffect(categorySlug, tag, sort) { load(reset = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding(),
    ) {
        IgngTopBar(
            title = title,
            subtitle = subtitle,
            onBack = onBack,
            actions = {
                IgngTagChip(text = "最新", selected = sort == "latest", onClick = { sort = "latest" })
                Spacer(Modifier.width(4.dp))
                IgngTagChip(text = "热门", selected = sort == "hot", onClick = { sort = "hot" })
            },
        )
        IgngPullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { scope.launch { load(reset = true) } },
            modifier = Modifier.weight(1f),
        ) {
            when {
                loading && posts.isEmpty() -> IgngLoadingCenter(loading = true)
                error != null && posts.isEmpty() -> Box(Modifier.padding(16.dp)) {
                    IgngErrorTile(message = error!!, onRetry = { scope.launch { load(reset = true) } })
                }
                posts.isEmpty() -> Box(Modifier.padding(16.dp)) {
                    IgngEmptyTile(title = "暂无相关文章", subtitle = "这里暂时还没有符合条件的内容。")
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(posts, key = { _, p -> p.id }) { idx, post ->
                        BbsPostRowCard(
                            post = post,
                            onClick = { onOpenPost(post.id) },
                            onAuthorClick = post.author?.id?.let { uid -> { onOpenUser(uid) } },
                            onTagClick = onOpenTag,
                            modifier = Modifier.igngBentoEnter(idx),
                        )
                    }
                    if (page < totalPages) {
                        item(key = "load_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                IgngTagChip(
                                    text = "加载更多",
                                    tone = IgngTone.ACCENT,
                                    onClick = { scope.launch { load(reset = false) } },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenColumn: (String) -> Unit,
    onOpenUser: (String) -> Unit,
    onOpenTag: (String) -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var hotTags by remember { mutableStateOf<List<TagStatDto>>(emptyList()) }
    var result by remember { mutableStateOf<SearchResponseDto?>(null) }
    var searching by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        when (val res = container.feedRepository.listTags(30)) {
            is ApiResult.Success -> hotTags = res.data.data
            is ApiResult.Failure -> Unit
        }
    }

    fun executeSearch(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return
        // Direct jump if user enters post numeric ID or igngbbs.net/post/<id>
        val directId = Regex("^(?:post_)?(\\d+)$").matchEntire(trimmed)?.groupValues?.get(1)?.toIntOrNull()
            ?: Regex("igngbbs\\.net/post/(\\d+)", RegexOption.IGNORE_CASE).find(trimmed)?.groupValues?.get(1)?.toIntOrNull()
        if (directId != null && trimmed.length <= 64) {
            onOpenPost(V1Id.post(directId))
            return
        }
        scope.launch {
            searching = true
            error = null
            when (val res = container.feedRepository.search(trimmed)) {
                is ApiResult.Success -> result = res.data
                is ApiResult.Failure -> error = res.error.message
            }
            searching = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回", tint = colors.textPrimary)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("关键词 / 文章编号 / igngbbs 链接", style = MaterialTheme.typography.bodyMedium) },
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
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = ""; result = null; error = null }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "清除", tint = colors.textMuted)
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { executeSearch(query) }),
            )
            IgngTagChip(text = "搜索", tone = IgngTone.ACCENT, onClick = { executeSearch(query) })
            Spacer(Modifier.width(4.dp))
        }

        when {
            searching -> IgngLoadingCenter(loading = true)
            error != null -> Box(Modifier.padding(16.dp)) {
                IgngErrorTile(message = error!!, onRetry = { executeSearch(query) })
            }
            result == null -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    IgngTile {
                        Text("热门标签", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                        Spacer(Modifier.height(10.dp))
                        if (hotTags.isEmpty()) {
                            Text("输入关键词开始搜索文章、专栏或作者。", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                hotTags.forEach { tag ->
                                    IgngTagChip(
                                        text = "#${tag.name} (${tag.count})",
                                        onClick = { onOpenTag(tag.name) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                val res = result!!
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (res.users.isNotEmpty()) {
                        item { IgngSectionHeader(title = "相关用户") }
                        items(res.users, key = { "u_${it.id}" }) { user ->
                            IgngTile(onClick = { onOpenUser(user.id) }, padding = PaddingValues(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    IgngAvatar(user.avatarUrl, user.displayName, size = 36.dp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(user.displayName, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                        Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                                    }
                                }
                            }
                        }
                    }
                    if (res.columns.isNotEmpty()) {
                        item { IgngSectionHeader(title = "相关专栏") }
                        items(res.columns, key = { "c_${it.id}" }) { col ->
                            IgngTile(onClick = { onOpenColumn(col.id) }, padding = PaddingValues(14.dp)) {
                                Text(col.name, style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
                                col.description?.takeIf { it.isNotBlank() }?.let { desc ->
                                    Spacer(Modifier.height(4.dp))
                                    Text(desc, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary, maxLines = 2)
                                }
                                Spacer(Modifier.height(6.dp))
                                Text("${col.postCount} 篇文章 · ${col.memberCount} 位成员", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                            }
                        }
                    }
                    item { IgngSectionHeader(title = "相关文章（${res.pagination.total}）") }
                    if (res.data.isEmpty()) {
                        item {
                            IgngEmptyTile(title = "没有匹配的文章", subtitle = "试试更换关键词，或直接输入文章编号跳转。")
                        }
                    } else {
                        items(res.data, key = { "p_${it.id}" }) { post ->
                            BbsPostRowCard(
                                post = post,
                                onClick = { onOpenPost(post.id) },
                                onAuthorClick = post.author?.id?.let { uid -> { onOpenUser(uid) } },
                                onTagClick = onOpenTag,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RulesScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    var rules by remember { mutableStateOf<BbsRulesDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var expandedClause by remember { mutableStateOf<String?>(null) }

    suspend fun load() {
        loading = true
        error = null
        when (val res = container.feedRepository.fetchRules()) {
            is ApiResult.Success -> rules = res.data
            is ApiResult.Failure -> error = res.error.message
        }
        loading = false
    }

    LaunchedEffect(Unit) { load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding(),
    ) {
        IgngTopBar(
            title = "社区规范与申诉准则",
            subtitle = rules?.let { "版本 ${it.version} · 更新于 ${it.updatedAt}" },
            onBack = onBack,
        )
        when {
            loading -> IgngLoadingCenter(loading = true)
            error != null -> Box(Modifier.padding(16.dp)) {
                IgngErrorTile(message = error!!, onRetry = { scope.launch { load() } })
            }
            rules != null -> {
                val r = rules!!
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (r.principles.isNotEmpty()) {
                        item {
                            IgngTile {
                                Text("基本原则", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Spacer(Modifier.height(8.dp))
                                r.principles.forEach { p ->
                                    Text(p.title, style = MaterialTheme.typography.titleSmall, color = colors.accent)
                                    Text(p.text, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                    item { IgngSectionHeader(title = "细则条款（${r.clauses.size}）") }
                    items(r.clauses, key = { it.code }) { clause ->
                        val isOpen = expandedClause == clause.code
                        IgngTile(onClick = { expandedClause = if (isOpen) null else clause.code }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "${clause.code} · ${clause.title}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f),
                                )
                                IgngTagChip(
                                    text = "S${clause.severity}",
                                    tone = if (clause.severity >= 3) IgngTone.DANGER else if (clause.severity == 2) IgngTone.WARNING else IgngTone.NEUTRAL,
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(clause.description, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                            AnimatedVisibility(visible = isOpen) {
                                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (clause.elements.isNotEmpty()) {
                                        Text("构成要件", style = MaterialTheme.typography.labelLarge, color = colors.textPrimary)
                                        clause.elements.forEach { e ->
                                            Text("• $e", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                        }
                                    }
                                    if (clause.excludes.isNotEmpty()) {
                                        Text("例外情形", style = MaterialTheme.typography.labelLarge, color = colors.success)
                                        clause.excludes.forEach { e ->
                                            Text("• $e", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                        }
                                    }
                                    if (clause.examples.violate.isNotEmpty()) {
                                        Text("违规示例", style = MaterialTheme.typography.labelLarge, color = colors.danger)
                                        clause.examples.violate.forEach { ex ->
                                            Text("× $ex", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                        }
                                    }
                                    if (clause.examples.allow.isNotEmpty()) {
                                        Text("允许示例", style = MaterialTheme.typography.labelLarge, color = colors.success)
                                        clause.examples.allow.forEach { ex ->
                                            Text("✓ $ex", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (r.appeal.isNotEmpty()) {
                        item {
                            IgngTile(soft = true) {
                                Text("申诉与复核机制", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                                Spacer(Modifier.height(8.dp))
                                r.appeal.forEach { item ->
                                    Text(item.title, style = MaterialTheme.typography.titleSmall, color = colors.accent)
                                    Text(item.text, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
