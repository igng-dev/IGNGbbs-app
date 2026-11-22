package net.igng.bbs.feature.inbox

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.designsystem.IgngEmptyTile
import net.igng.bbs.core.designsystem.IgngErrorTile
import net.igng.bbs.core.designsystem.IgngLoadingCenter
import net.igng.bbs.core.designsystem.IgngPullToRefreshBox
import net.igng.bbs.core.designsystem.IgngRadius
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTile
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.designsystem.IgngTopBar
import net.igng.bbs.core.model.BbsFormatters
import net.igng.bbs.core.model.InboxItemDto
import net.igng.bbs.core.network.ApiResult

private val INBOX_TABS = listOf(
    "all" to "全部",
    "mention" to "提及",
    "reply" to "回复",
    "system" to "系统",
    "column" to "专栏",
)

@Composable
fun InboxScreen(
    container: BbsAppContainer,
    onBack: () -> Unit,
    onOpenPost: (String) -> Unit,
    onRequireLogin: () -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val activeAccount by container.sessionStore.activeAccount.collectAsState()

    var tab by remember { mutableStateOf("all") }
    var unreadOnly by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<InboxItemDto>>(emptyList()) }
    var unreadTotal by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var detailItem by remember { mutableStateOf<InboxItemDto?>(null) }

    suspend fun load(isRefresh: Boolean) {
        if (activeAccount == null) {
            loading = false
            return
        }
        if (isRefresh) refreshing = true else if (items.isEmpty()) loading = true
        error = null
        when (val res = container.inboxRepository.listInbox(
            tab = tab,
            query = query.trim().ifBlank { null },
            unreadOnly = unreadOnly,
        )) {
            is ApiResult.Success -> {
                items = res.data.data
                unreadTotal = res.data.unread.total
            }
            is ApiResult.Failure -> if (items.isEmpty()) error = res.error.message
        }
        loading = false
        refreshing = false
    }

    LaunchedEffect(tab, unreadOnly, activeAccount?.userId) { load(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.canvas)
            .statusBarsPadding(),
    ) {
        IgngTopBar(
            title = "消息收件箱",
            subtitle = if (unreadTotal > 0) "$unreadTotal 条未读" else null,
            onBack = onBack,
            actions = {
                if (activeAccount != null && unreadTotal > 0) {
                    IconButton(onClick = {
                        scope.launch {
                            container.inboxRepository.markAllRead("all")
                            container.refreshBootstrap()
                            load(true)
                        }
                    }) {
                        Icon(Icons.Rounded.DoneAll, contentDescription = "全部标为已读", tint = colors.accent)
                    }
                }
            },
        )

        if (activeAccount == null) {
            Box(Modifier.padding(16.dp)) {
                IgngEmptyTile(
                    title = "登录后查看消息通知",
                    subtitle = "系统通知、评论回复、@提及与专栏动态都会汇集在这里。",
                    actionLabel = "登录 IGNG 账户",
                    onAction = onRequireLogin,
                )
            }
            return
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("搜索通知正文或标题", style = MaterialTheme.typography.bodySmall) },
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
            IgngTagChip(text = "查找", tone = IgngTone.ACCENT, onClick = { scope.launch { load(true) } })
        }
        Spacer(Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(INBOX_TABS, key = { it.first }) { (key, label) ->
                IgngTagChip(text = label, selected = tab == key, onClick = { tab = key })
            }
            item {
                IgngTagChip(
                    text = if (unreadOnly) "仅未读 ✓" else "仅未读",
                    selected = unreadOnly,
                    tone = IgngTone.WARNING,
                    onClick = { unreadOnly = !unreadOnly },
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
                loading && items.isEmpty() -> IgngLoadingCenter(loading = true)
                error != null && items.isEmpty() -> Box(Modifier.padding(16.dp)) {
                    IgngErrorTile(message = error!!, onRetry = { scope.launch { load(false) } })
                }
                items.isEmpty() -> Box(Modifier.padding(16.dp)) {
                    IgngEmptyTile(
                        title = if (unreadOnly) "没有未读消息" else "收件箱很安静",
                        subtitle = "当有人回复或 @提到你，或者有新的系统通知时会立刻出现在这里。",
                    )
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(items, key = { it.id }) { item ->
                        IgngTile(
                            soft = item.isRead,
                            padding = PaddingValues(14.dp),
                            onClick = {
                                if (!item.isRead) {
                                    items = items.map { if (it.id == item.id) it.copy(isRead = true) else it }
                                    scope.launch {
                                        container.inboxRepository.markItemRead(item.id)
                                        container.refreshBootstrap()
                                    }
                                }
                                if (!item.postId.isNullOrBlank()) {
                                    onOpenPost(item.postId!!)
                                } else {
                                    detailItem = item
                                }
                            },
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!item.isRead) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(colors.accent),
                                    )
                                }
                                IgngTagChip(
                                    text = when (item.kind) {
                                        "mention" -> "提及"
                                        "reply" -> "回复"
                                        "column" -> "专栏"
                                        else -> "系统"
                                    },
                                    tone = if (item.kind == "mention") IgngTone.ACCENT else IgngTone.NEUTRAL,
                                )
                                Text(
                                    text = BbsFormatters.relativeTime(item.createdAt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textMuted,
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.SemiBold,
                                ),
                                color = colors.textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (item.content.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = item.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textSecondary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    detailItem?.let { item ->
        AlertDialog(
            onDismissRequest = { detailItem = null },
            title = { Text(item.title) },
            text = { Text(item.content.ifBlank { "无更多正文内容" }) },
            confirmButton = { TextButton(onClick = { detailItem = null }) { Text("知道了") } },
        )
    }
}
