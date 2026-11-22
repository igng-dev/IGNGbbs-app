package net.igng.bbs.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Core IGNG Bento 2.0 tile container: rounded surface with hairline 1dp border, zero heavy elevation.
 */
@Composable
fun IgngTile(
    modifier: Modifier = Modifier,
    soft: Boolean = false,
    padding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = IgngTheme.colors
    val shape = RoundedCornerShape(IgngRadius.tile)
    val baseModifier = if (onClick != null) {
        modifier.igngPressScale(onClick = onClick)
    } else {
        modifier
    }
    Column(
        modifier = baseModifier
            .clip(shape)
            .background(if (soft) colors.tileSoft else colors.tile)
            .border(1.dp, colors.line, shape)
            .padding(padding),
        content = content,
    )
}

@Composable
fun IgngTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    val colors = IgngTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "返回",
                    tint = colors.textPrimary,
                )
            }
            Spacer(Modifier.width(4.dp))
        } else {
            Spacer(Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions()
        }
    }
}

@Composable
fun IgngSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = IgngTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = colors.accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(IgngRadius.pill))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}

enum class IgngTone {
    NEUTRAL,
    ACCENT,
    SUCCESS,
    WARNING,
    DANGER,
}

@Composable
fun IgngTagChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: IgngTone = IgngTone.NEUTRAL,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = IgngTheme.colors
    val intensity = IgngTheme.motionIntensity
    val targetBg = when {
        selected -> colors.accent
        tone == IgngTone.ACCENT -> colors.accentLow
        tone == IgngTone.SUCCESS -> colors.successLow
        tone == IgngTone.WARNING -> colors.warningLow
        tone == IgngTone.DANGER -> colors.dangerLow
        else -> colors.tileSoft
    }
    val targetFg = when {
        selected -> colors.onAccent
        tone == IgngTone.ACCENT -> colors.accent
        tone == IgngTone.SUCCESS -> colors.success
        tone == IgngTone.WARNING -> colors.warning
        tone == IgngTone.DANGER -> colors.danger
        else -> colors.textSecondary
    }
    val bg by animateColorAsState(targetBg, IgngMotion.easeTween(160, intensity), label = "chipBg")
    val fg by animateColorAsState(targetFg, IgngMotion.easeTween(160, intensity), label = "chipFg")
    val shape = RoundedCornerShape(IgngRadius.pill)
    val mod = if (onClick != null) modifier.igngPressScale(haptic = true, onClick = onClick) else modifier

    Box(
        modifier = mod
            .clip(shape)
            .background(bg)
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else colors.line,
                shape = shape,
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun IgngAvatar(
    avatarUrl: String?,
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    onClick: (() -> Unit)? = null,
) {
    val colors = IgngTheme.colors
    val initial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "I"
    val mod = (if (onClick != null) modifier.igngPressScale(onClick = onClick) else modifier)
        .size(size)
        .clip(CircleShape)
        .background(colors.accentLow)
        .border(1.dp, colors.line, CircleShape)

    Box(modifier = mod, contentAlignment = Alignment.Center) {
        Text(
            text = initial,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = colors.accent,
        )
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
        }
    }
}

@Composable
fun IgngPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val colors = IgngTheme.colors
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(IgngRadius.control),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accent,
            contentColor = colors.onAccent,
            disabledContainerColor = colors.lineStrong,
            disabledContentColor = colors.textMuted,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun IgngSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    val colors = IgngTheme.colors
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(IgngRadius.control),
        border = BorderStroke(1.dp, colors.lineStrong),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = colors.tile,
            contentColor = colors.textPrimary,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Distinct error state card (never masquerades as empty state or zero counts on network failure).
 */
@Composable
fun IgngErrorTile(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "暂时无法获取数据",
) {
    val colors = IgngTheme.colors
    IgngTile(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        Spacer(Modifier.height(14.dp))
        IgngSecondaryButton(
            text = "重新加载",
            icon = Icons.Rounded.Refresh,
            onClick = onRetry,
        )
    }
}

@Composable
fun IgngEmptyTile(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = IgngTheme.colors
    IgngTile(modifier = modifier.fillMaxWidth(), soft = true) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
        if (!actionLabel.isNullOrBlank() && onAction != null) {
            Spacer(Modifier.height(14.dp))
            IgngPrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun IgngStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    tone: IgngTone = IgngTone.NEUTRAL,
) {
    val colors = IgngTheme.colors
    val valueColor = when (tone) {
        IgngTone.ACCENT -> colors.accent
        IgngTone.SUCCESS -> colors.success
        IgngTone.WARNING -> colors.warning
        IgngTone.DANGER -> colors.danger
        IgngTone.NEUTRAL -> colors.textPrimary
    }
    IgngTile(modifier = modifier, padding = PaddingValues(14.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"),
            color = valueColor,
            maxLines = 1,
        )
        if (!hint.isNullOrBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun IgngInfoBanner(
    title: String,
    message: String,
    tone: IgngTone = IgngTone.ACCENT,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colors = IgngTheme.colors
    val bg = when (tone) {
        IgngTone.ACCENT -> colors.accentLow
        IgngTone.SUCCESS -> colors.successLow
        IgngTone.WARNING -> colors.warningLow
        IgngTone.DANGER -> colors.dangerLow
        IgngTone.NEUTRAL -> colors.tileSoft
    }
    val fg = when (tone) {
        IgngTone.ACCENT -> colors.accent
        IgngTone.SUCCESS -> colors.success
        IgngTone.WARNING -> colors.warning
        IgngTone.DANGER -> colors.danger
        IgngTone.NEUTRAL -> colors.textPrimary
    }
    val shape = RoundedCornerShape(IgngRadius.card)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = bg,
        border = BorderStroke(1.dp, fg.copy(alpha = 0.25f)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = fg)
            Spacer(Modifier.height(4.dp))
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = colors.textPrimary)
            if (!actionLabel.isNullOrBlank() && onAction != null) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = fg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(IgngRadius.pill))
                        .clickable(onClick = onAction)
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                )
            }
        }
    }
}
