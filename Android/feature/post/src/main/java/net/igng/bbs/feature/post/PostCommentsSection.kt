package net.igng.bbs.feature.post

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.ThumbUpOffAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.data.CreateCommentRequest
import net.igng.bbs.core.designsystem.IgngAvatar
import net.igng.bbs.core.designsystem.IgngEmptyTile
import net.igng.bbs.core.designsystem.IgngErrorTile
import net.igng.bbs.core.designsystem.IgngInfoBanner
import net.igng.bbs.core.designsystem.IgngRadius
import net.igng.bbs.core.designsystem.IgngSectionHeader
import net.igng.bbs.core.designsystem.IgngTagChip
import net.igng.bbs.core.designsystem.IgngTheme
import net.igng.bbs.core.designsystem.IgngTile
import net.igng.bbs.core.designsystem.IgngTone
import net.igng.bbs.core.model.BbsFormatters
import net.igng.bbs.core.model.CommentDto
import net.igng.bbs.core.model.UserBriefDto
import net.igng.bbs.core.network.ApiResult

@Composable
fun PostCommentsSection(
    container: BbsAppContainer,
    postId: String,
    onOpenUser: (String) -> Unit,
    onReportComment: (String) -> Unit,
    onRequireLogin: () -> Unit,
) {
    val colors = IgngTheme.colors
    val scope = rememberCoroutineScope()
    val activeAccount by container.sessionStore.activeAccount.collectAsState()

    var sortBy by remember { mutableStateOf("oldest") }
    var comments by remember { mutableStateOf<List<CommentDto>>(emptyList()) }
    var latestCommentId by remember { mutableStateOf<String?>(null) }
    var hasNewComments by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var draftText by remember { mutableStateOf("") }
    var replyTarget by remember { mutableStateOf<CommentDto?>(null) }
    var reEditTarget by remember { mutableStateOf<CommentDto?>(null) }
    var sending by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf<String?>(null) }

    // @mention picker state
    var showMentionPicker by remember { mutableStateOf(false) }
    var mentionQuery by remember { mutableStateOf("") }
    var mentionCandidates by remember { mutableStateOf<List<UserBriefDto>>(emptyList()) }
    var selectedMentionIds by remember { mutableStateOf<List<String>>(emptyList()) }

    val expandedFolded = remember { mutableStateMapOf<String, Boolean>() }

    suspend fun loadComments() {
        error = null
        when (val res = container.postRepository.listComments(postId = postId, sortBy = sortBy, pageSize = 50)) {
            is ApiResult.Success -> {
                comments = res.data.data
                latestCommentId = res.data.latestCommentId
                hasNewComments = false
            }
            is ApiResult.Failure -> error = res.error.message
        }
    }

    LaunchedEffect(postId, sortBy, activeAccount?.userId) { loadComments() }

    // Subtle 15-second poll for new comments without interrupting scroll
    LaunchedEffect(postId, latestCommentId) {
        while (true) {
            delay(15_000L)
            val anchor = latestCommentId ?: continue
            when (val res = container.postRepository.listComments(postId = postId, pageSize = 5, afterId = anchor)) {
                is ApiResult.Success -> {
                    if (res.data.data.isNotEmpty() || (res.data.latestCommentId != null && res.data.latestCommentId != anchor)) {
                        hasNewComments = true
                    }
                }
                is ApiResult.Failure -> Unit
            }
        }
    }

    LaunchedEffect(mentionQuery, showMentionPicker) {
        if (!showMentionPicker || mentionQuery.trim().isEmpty()) {
            mentionCandidates = emptyList()
            return@LaunchedEffect
        }
        delay(220L)
        when (val res = container.postRepository.searchUsers(mentionQuery.trim(), 8)) {
            is ApiResult.Success -> mentionCandidates = res.data.data
            is ApiResult.Failure -> Unit
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        HorizontalDivider(color = colors.line)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IgngSectionHeader(title = "讨论区（${comments.size}）", modifier = Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IgngTagChip(text = "最早", selected = sortBy == "oldest", onClick = { sortBy = "oldest" })
                IgngTagChip(text = "最新", selected = sortBy == "newest", onClick = { sortBy = "newest" })
                IgngTagChip(text = "最热", selected = sortBy == "hot", onClick = { sortBy = "hot" })
            }
        }

        AnimatedVisibility(visible = hasNewComments) {
            IgngInfoBanner(
                title = "有新评论发表",
                message = "点击刷新即可查看最新讨论，不会打断当前阅读位置。",
                tone = IgngTone.ACCENT,
                actionLabel = "立即刷新",
                onAction = { scope.launch { loadComments() } },
            )
        }

        notice?.let { msg ->
            IgngInfoBanner(title = "评论状态", message = msg, tone = IgngTone.SUCCESS)
        }

        // Composer card
        IgngTile(padding = PaddingValues(14.dp)) {
            if (activeAccount == null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("登录后参与讨论与 @提及互动", style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    IgngTagChip(text = "去登录", tone = IgngTone.ACCENT, onClick = onRequireLogin)
                }
            } else {
                replyTarget?.let { target ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("回复 ${target.author?.displayName ?: "评论"}", style = MaterialTheme.typography.labelMedium, color = colors.accent)
                        Text("取消", style = MaterialTheme.typography.labelMedium, color = colors.textMuted, modifier = Modifier.clickable { replyTarget = null })
                    }
                    Spacer(Modifier.height(6.dp))
                }
                reEditTarget?.let { target ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("修改未通过的评论并重新送审", style = MaterialTheme.typography.labelMedium, color = colors.warning)
                        Text("取消", style = MaterialTheme.typography.labelMedium, color = colors.textMuted, modifier = Modifier.clickable { reEditTarget = null; draftText = "" })
                    }
                    Spacer(Modifier.height(6.dp))
                }

                OutlinedTextField(
                    value = draftText,
                    onValueChange = { draftText = it },
                    placeholder = { Text("写下你的看法，支持 @提及用户…", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5,
                    shape = RoundedCornerShape(IgngRadius.control),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.line,
                    ),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    IconButton(onClick = { showMentionPicker = !showMentionPicker }) {
                        Icon(Icons.Rounded.AlternateEmail, contentDescription = "提及用户", tint = colors.accent)
                    }
                    IgngTagChip(
                        text = if (sending) "发送中…" else if (reEditTarget != null) "重提审核" else "发表评论",
                        tone = IgngTone.ACCENT,
                        selected = true,
                        onClick = {
                            val text = draftText.trim()
                            if (text.isEmpty() || sending) return@IgngTagChip
                            sending = true
                            scope.launch {
                                when (val res = container.postRepository.createComment(
                                    postId = postId,
                                    request = CreateCommentRequest(
                                        content = text,
                                        parentId = replyTarget?.id,
                                        reEditCommentId = reEditTarget?.id,
                                        mentionUserIds = selectedMentionIds.takeIf { it.isNotEmpty() },
                                    ),
                                )) {
                                    is ApiResult.Success -> {
                                        draftText = ""
                                        replyTarget = null
                                        reEditTarget = null
                                        selectedMentionIds = emptyList()
                                        showMentionPicker = false
                                        notice = if (res.data.reviewPending) "评论已提交，正在等待审核" else "评论发表成功"
                                        loadComments()
                                    }
                                    is ApiResult.Failure -> notice = res.error.message
                                }
                                sending = false
                            }
                        },
                    )
                }

                AnimatedVisibility(visible = showMentionPicker) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        OutlinedTextField(
                            value = mentionQuery,
                            onValueChange = { mentionQuery = it },
                            placeholder = { Text("输入用户名或昵称搜索 @提及对象", style = MaterialTheme.typography.bodySmall) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        mentionCandidates.forEach { u ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(IgngRadius.control))
                                    .background(colors.tileSoft)
                                    .clickable {
                                        draftText = "$draftText @${u.username} ".trimStart()
                                        selectedMentionIds = (selectedMentionIds + u.id).distinct()
                                        mentionQuery = ""
                                        showMentionPicker = false
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                IgngAvatar(u.avatarUrl, u.displayName, size = 24.dp)
                                Text("${u.displayName} (@${u.username})", style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }

        if (error != null) {
            IgngErrorTile(message = error!!, onRetry = { scope.launch { loadComments() } })
        } else if (comments.isEmpty()) {
            IgngEmptyTile(title = "还没有评论", subtitle = "欢迎发表第一条观点。")
        } else {
            comments.forEach { comment ->
                CommentItemCard(
                    comment = comment,
                    isFoldedExpanded = expandedFolded[comment.id] == true,
                    onToggleFold = { expandedFolded[comment.id] = !(expandedFolded[comment.id] ?: false) },
                    onOpenUser = onOpenUser,
                    onReply = {
                        if (activeAccount == null) onRequireLogin() else {
                            reEditTarget = null
                            replyTarget = it
                        }
                    },
                    onReEdit = {
                        replyTarget = null
                        reEditTarget = it
                        draftText = it.content
                    },
                    onLike = { target ->
                        if (activeAccount == null) { onRequireLogin(); return@CommentItemCard }
                        scope.launch {
                            when (val r = container.postRepository.toggleCommentLike(target.id)) {
                                is ApiResult.Success -> loadComments()
                                is ApiResult.Failure -> notice = r.error.message
                            }
                        }
                    },
                    onDelete = { target ->
                        scope.launch {
                            when (val r = container.postRepository.deleteComment(target.id)) {
                                is ApiResult.Success -> { notice = "评论已删除"; loadComments() }
                                is ApiResult.Failure -> notice = r.error.message
                            }
                        }
                    },
                    onReport = onReportComment,
                )
            }
        }
    }
}

@Composable
private fun CommentItemCard(
    comment: CommentDto,
    isFoldedExpanded: Boolean,
    onToggleFold: () -> Unit,
    onOpenUser: (String) -> Unit,
    onReply: (CommentDto) -> Unit,
    onReEdit: (CommentDto) -> Unit,
    onLike: (CommentDto) -> Unit,
    onDelete: (CommentDto) -> Unit,
    onReport: (String) -> Unit,
    isReply: Boolean = false,
) {
    val colors = IgngTheme.colors
    IgngTile(
        soft = isReply,
        padding = PaddingValues(13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IgngAvatar(
                avatarUrl = comment.author?.avatarUrl,
                displayName = comment.author?.displayName ?: "用户",
                size = if (isReply) 22.dp else 26.dp,
                onClick = comment.author?.id?.let { uid -> { onOpenUser(uid) } },
            )
            Text(
                text = comment.author?.displayName ?: "匿名用户",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = colors.textPrimary,
            )
            Text(
                text = BbsFormatters.relativeTime(comment.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
            )
            Spacer(Modifier.weight(1f))
            if (comment.status == 2) {
                IgngTagChip(text = "审核中", tone = IgngTone.WARNING)
            } else if (comment.status == 1) {
                IgngTagChip(text = "未通过", tone = IgngTone.DANGER)
            }
        }

        Spacer(Modifier.height(6.dp))

        if (comment.folded && !isFoldedExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(IgngRadius.control))
                    .background(colors.tileSoft)
                    .clickable(onClick = onToggleFold)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("该评论已被社区规则折叠", style = MaterialTheme.typography.bodySmall, color = colors.textMuted)
                Text("展开查看", style = MaterialTheme.typography.labelMedium, color = colors.accent)
            }
        } else {
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
            )
            comment.moderation?.summary?.takeIf { it.isNotBlank() }?.let { modSummary ->
                Spacer(Modifier.height(4.dp))
                Text("审核提示：$modSummary", style = MaterialTheme.typography.bodySmall, color = colors.warning)
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.clickable { onLike(comment) },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = if (comment.isLiked) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
                    contentDescription = "点赞",
                    tint = if (comment.isLiked) colors.accent else colors.textMuted,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = comment.likeCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = if (comment.isLiked) colors.accent else colors.textMuted,
                )
            }
            if (!isReply) {
                Row(
                    modifier = Modifier.clickable { onReply(comment) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Reply, contentDescription = "回复", tint = colors.textMuted, modifier = Modifier.size(16.dp))
                    Text("回复", style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
                }
            }
            if (comment.canReEdit) {
                Row(
                    modifier = Modifier.clickable { onReEdit(comment) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = "修改重提", tint = colors.warning, modifier = Modifier.size(16.dp))
                    Text("修改重提", style = MaterialTheme.typography.labelSmall, color = colors.warning)
                }
            }
            Spacer(Modifier.weight(1f))
            if (comment.canDelete) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "删除",
                    tint = colors.danger,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDelete(comment) },
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Flag,
                    contentDescription = "举报",
                    tint = colors.textMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onReport(comment.id) },
                )
            }
        }

        if (comment.replies.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                comment.replies.forEach { reply ->
                    CommentItemCard(
                        comment = reply,
                        isFoldedExpanded = true,
                        onToggleFold = {},
                        onOpenUser = onOpenUser,
                        onReply = onReply,
                        onReEdit = onReEdit,
                        onLike = onLike,
                        onDelete = onDelete,
                        onReport = onReport,
                        isReply = true,
                    )
                }
            }
        }
    }
}
