package net.igng.bbs.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material.icons.rounded.ThumbUpOffAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.igng.bbs.core.model.BbsFormatters
import net.igng.bbs.core.model.LocalReadingHistoryEntry
import net.igng.bbs.core.model.PostSummaryDto
import net.igng.bbs.core.model.RecommendationSlotDto
import net.igng.bbs.core.model.RiskSummaryDto

/**
 * Floating 4-tab frosted glass Bento pill navigation bar with subtle sliding pill highlight.
 */
data class BbsNavTabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
)

@Composable
fun BbsFloatingNavBar(
    tabs: List<BbsNavTabItem>,
    selectedRoute: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    val intensity = IgngTheme.motionIntensity
    val shape = RoundedCornerShape(IgngRadius.pill)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(shape)
                .background(colors.glassBar)
                .border(1.dp, colors.lineStrong, shape)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val selected = tab.route == selectedRoute
                val bg by animateColorAsState(
                    targetValue = if (selected) colors.accentLow else Color.Transparent,
                    animationSpec = IgngMotion.easeTween(180, intensity),
                    label = "navBg",
                )
                val fg by animateColorAsState(
                    targetValue = if (selected) colors.accent else colors.textSecondary,
                    animationSpec = IgngMotion.easeTween(180, intensity),
                    label = "navFg",
                )
                val horizPad by animateDpAsState(
                    targetValue = if (selected) 16.dp else 12.dp,
                    animationSpec = IgngMotion.standardSpring(intensity),
                    label = "navPad",
                )
                Row(
                    modifier = Modifier
                        .clip(shape)
                        .background(bg)
                        .igngPressScale(haptic = true, onClick = { onSelect(tab.route) })
                        .padding(horizontal = horizPad, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = fg,
                            modifier = Modifier.size(20.dp),
                        )
                        if (tab.badgeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.danger),
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = selected,
                        enter = fadeIn(IgngMotion.easeTween(160, intensity)),
                        exit = fadeOut(IgngMotion.easeTween(120, intensity)),
                    ) {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = fg,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Horizontal Bento recommendations carousel ("推荐速览") with type badges and human-readable reasons.
 */
@Composable
fun BbsRecommendRail(
    slots: List<RecommendationSlotDto>,
    onPostClick: (PostSummaryDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    if (slots.isEmpty()) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(slots, key = { _, slot -> "rec_${slot.post.id}_${slot.type}" }) { index, slot ->
            val typeLabel = when (slot.type) {
                "hot" -> "社区热议"
                "trending" -> "本周升温"
                "discussed" -> "讨论正热"
                "updated" -> "刚刚更新"
                "newcomer" -> "新作者首作"
                else -> "最新发布"
            }
            val tone = when (slot.type) {
                "hot" -> IgngTone.ACCENT
                "trending" -> IgngTone.SUCCESS
                "discussed" -> IgngTone.WARNING
                else -> IgngTone.NEUTRAL
            }
            IgngTile(
                modifier = Modifier
                    .width(268.dp)
                    .igngBentoEnter(index),
                padding = PaddingValues(15.dp),
                onClick = { onPostClick(slot.post) },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IgngTagChip(text = typeLabel, tone = tone)
                    Text(
                        text = slot.reason,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = slot.post.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.height(44.dp),
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IgngAvatar(
                        avatarUrl = slot.post.author?.avatarUrl,
                        displayName = slot.post.author?.displayName ?: "作者",
                        size = 22.dp,
                    )
                    Text(
                        text = slot.post.author?.displayName ?: "匿名用户",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    slot.post.category?.name?.let { cat ->
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact "继续阅读" Bento card displaying local reading progress bar and 1-tap resume.
 */
@Composable
fun BbsContinueReadingCard(
    entry: LocalReadingHistoryEntry,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    IgngTile(
        modifier = modifier.fillMaxWidth(),
        soft = true,
        padding = PaddingValues(14.dp),
        onClick = onResume,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "继续阅读",
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${(entry.progressRatio * 100).toInt().coerceIn(0, 100)}%",
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                color = colors.textMuted,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = entry.title,
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { entry.progressRatio.coerceIn(0.03f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(IgngRadius.pill)),
            color = colors.accent,
            trackColor = colors.line,
        )
    }
}

/**
 * Primary Bento post row card used across Home feed, Category, Tag, Column, Search, and Profile.
 */
@Composable
fun BbsPostRowCard(
    post: PostSummaryDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onAuthorClick: (() -> Unit)? = null,
    onTagClick: ((String) -> Unit)? = null,
    showModerationBadge: Boolean = false,
) {
    val colors = IgngTheme.colors
    IgngTile(
        modifier = modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
        onClick = onClick,
    ) {
        // Top meta row: Author + relative time + pinned/status badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IgngAvatar(
                avatarUrl = post.author?.avatarUrl,
                displayName = post.author?.displayName ?: "作者",
                size = 24.dp,
                onClick = onAuthorClick,
            )
            Text(
                text = post.author?.displayName ?: "匿名用户",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .let { if (onAuthorClick != null) it.clickable(onClick = onAuthorClick) else it },
            )
            Text(
                text = "· ${BbsFormatters.relativeTime(post.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
            )
            Spacer(Modifier.weight(1f))
            if (post.isPinned) {
                IgngTagChip(text = "置顶", tone = IgngTone.ACCENT)
            }
            if (showModerationBadge) {
                val badgeTone = when {
                    post.status == 1 -> IgngTone.NEUTRAL
                    post.moderationStatus == "pending" -> IgngTone.WARNING
                    post.moderationStatus == "rejected" || post.banStatus in listOf(2, 4) -> IgngTone.DANGER
                    else -> IgngTone.SUCCESS
                }
                IgngTagChip(
                    text = BbsFormatters.postStatusLabel(post.status, post.banStatus, post.moderationStatus),
                    tone = badgeTone,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Title
        Text(
            text = post.title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        // Excerpt / Summary
        val snippet = post.summary?.takeIf { it.isNotBlank() } ?: post.excerpt
        if (snippet.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = snippet,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Category / Column / Tags pill row
        val hasMetaChips = post.category != null || post.column != null || post.tags.isNotEmpty()
        if (hasMetaChips) {
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                post.category?.let { cat ->
                    IgngTagChip(text = cat.name, tone = IgngTone.ACCENT)
                }
                post.column?.let { col ->
                    IgngTagChip(text = col.name, tone = IgngTone.NEUTRAL)
                }
                post.tags.take(2).forEach { tag ->
                    IgngTagChip(
                        text = "#$tag",
                        tone = IgngTone.NEUTRAL,
                        onClick = onTagClick?.let { fn -> { fn(tag) } },
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Tabular metrics row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            MetricMini(icon = Icons.Rounded.RemoveRedEye, count = post.views)
            MetricMini(icon = Icons.Rounded.ThumbUpOffAlt, count = post.likeCount)
            MetricMini(icon = Icons.AutoMirrored.Rounded.Comment, count = post.commentCount)
            Spacer(Modifier.weight(1f))
            post.series?.let { ser ->
                Text(
                    text = "系列 · ${ser.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MetricMini(icon: ImageVector, count: Int) {
    val colors = IgngTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(15.dp))
        Text(
            text = BbsFormatters.compactCount(count),
            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
            color = colors.textMuted,
        )
    }
}

/**
 * Collapsible series group card with smooth spring accordion expansion.
 */
@Composable
fun BbsSeriesCollapseGroup(
    seriesName: String,
    posts: List<PostSummaryDto>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onPostClick: (PostSummaryDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    val intensity = IgngTheme.motionIntensity
    IgngTile(modifier = modifier.fillMaxWidth(), soft = true, padding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .igngPressScale(haptic = true, onClick = onToggle),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "系列 · $seriesName",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.accent,
                )
                Text(
                    text = "共 ${posts.size} 篇文章 · 点击${if (expanded) "收起" else "展开"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
                tint = colors.textSecondary,
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(IgngMotion.standardSpring(intensity)) + fadeIn(IgngMotion.easeTween(180, intensity)),
            exit = shrinkVertically(IgngMotion.standardSpring(intensity)) + fadeOut(IgngMotion.easeTween(140, intensity)),
        ) {
            Column(
                modifier = Modifier.padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                posts.forEach { post ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(IgngRadius.control))
                            .background(colors.tile)
                            .border(1.dp, colors.line, RoundedCornerShape(IgngRadius.control))
                            .igngPressScale(onClick = { onPostClick(post) })
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = post.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = BbsFormatters.compactCount(post.views),
                            style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                            color = colors.textMuted,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Auto-hiding floating article action bar with smooth spring heart/bookmark tactile animations.
 */
@Composable
fun BbsFloatingArticleBar(
    visible: Boolean,
    liked: Boolean,
    likeCount: Int,
    favorited: Boolean,
    commentCount: Int,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    val intensity = IgngTheme.motionIntensity
    val shape = RoundedCornerShape(IgngRadius.pill)

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(IgngMotion.standardSpring(intensity)) { it } + fadeIn(IgngMotion.easeTween(160, intensity)),
        exit = slideOutVertically(IgngMotion.standardSpring(intensity)) { it } + fadeOut(IgngMotion.easeTween(140, intensity)),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .clip(shape)
                    .background(colors.glassBar)
                    .border(1.dp, colors.lineStrong, shape)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val likeScale by animateFloatAsState(
                    targetValue = if (liked) 1.12f else 1f,
                    animationSpec = IgngMotion.quickSpring(intensity),
                    label = "likeScale",
                )
                val favScale by animateFloatAsState(
                    targetValue = if (favorited) 1.12f else 1f,
                    animationSpec = IgngMotion.quickSpring(intensity),
                    label = "favScale",
                )

                ActionPillItem(
                    icon = if (liked) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
                    label = BbsFormatters.compactCount(likeCount),
                    tint = if (liked) colors.accent else colors.textSecondary,
                    iconScale = likeScale,
                    onClick = onLike,
                )
                ActionPillItem(
                    icon = if (favorited) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    label = if (favorited) "已收藏" else "收藏",
                    tint = if (favorited) colors.warning else colors.textSecondary,
                    iconScale = favScale,
                    onClick = onFavorite,
                )
                ActionPillItem(
                    icon = Icons.AutoMirrored.Rounded.Comment,
                    label = BbsFormatters.compactCount(commentCount),
                    tint = colors.textSecondary,
                    onClick = onComment,
                )
                ActionPillItem(
                    icon = Icons.Rounded.Share,
                    label = "分享",
                    tint = colors.textSecondary,
                    onClick = onShare,
                )
            }
        }
    }
}

@Composable
private fun ActionPillItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    iconScale: Float = 1f,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(IgngRadius.pill))
            .igngPressScale(haptic = true, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                },
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
            color = tint,
        )
    }
}

/**
 * Dual-domain risk control visual meter (ARTICLE 30% rejection ratio + COMMENT 3-strike meter).
 */
@Composable
fun BbsRiskMeterCard(
    risk: RiskSummaryDto,
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    IgngTile(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "双域内容风控状态",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(10.dp))

        // Article domain
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "文章域（近 ${risk.article.windowDays} 天）", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            IgngTagChip(
                text = if (risk.article.active) "风控中（先审后发）" else "正常（先发后审）",
                tone = if (risk.article.active) IgngTone.WARNING else IgngTone.SUCCESS,
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { risk.article.rejectionRate.toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(IgngRadius.pill)),
            color = if (risk.article.active) colors.warning else colors.success,
            trackColor = colors.line,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "通过 ${risk.article.approved} · 未通过 ${risk.article.rejected} · 拒绝率 ${(risk.article.rejectionRate * 100).toInt()}%（≥30% 且样本≥3 触发）",
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            color = colors.textMuted,
        )

        Spacer(Modifier.height(14.dp))

        // Comment domain
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = "评论域（近 ${risk.comment.windowDays} 天）", style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            IgngTagChip(
                text = if (risk.comment.active) "风控中（先审后发）" else "正常（即发即显）",
                tone = if (risk.comment.active) IgngTone.WARNING else IgngTone.SUCCESS,
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { (risk.comment.strikes / 3f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(IgngRadius.pill)),
            color = if (risk.comment.active) colors.warning else colors.success,
            trackColor = colors.line,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "近 30 天举报成立：${risk.comment.strikes} / 3 次",
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            color = colors.textMuted,
        )
    }
}
