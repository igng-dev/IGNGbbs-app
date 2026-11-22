package net.igng.bbs.core.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import net.igng.bbs.core.model.MotionIntensity
import kotlin.math.PI
import kotlin.math.sin

/**
 * IGNG Folded Core ("折角核心") brand loading indicator.
 * Pure graphic-only motion (no "加载中…" text clutter) with a 1700ms calm rhythmic pulse.
 */
@Composable
fun IgngFoldedCoreGlyph(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    color: Color = IgngTheme.colors.accent,
) {
    val intensity = IgngTheme.motionIntensity
    val phase = if (intensity == MotionIntensity.REDUCED) {
        0.25f
    } else {
        val transition = rememberInfiniteTransition(label = "foldedCore")
        val value by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1700, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "foldedCorePhase",
        )
        value
    }

    val breathe = 0.88f + 0.12f * sin(phase * 2f * PI).toFloat()
    val angle = if (intensity == MotionIntensity.FULL) phase * 360f else 0f

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = (w * 0.085f).coerceAtLeast(2.2f)
        val inset = stroke * 1.15f
        val boxSize = (w - inset * 2f) * breathe
        val origin = (w - boxSize) / 2f

        // Outer subtle Bento frame
        drawRoundRect(
            color = color.copy(alpha = 0.18f),
            topLeft = Offset(origin, origin),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(boxSize * 0.26f, boxSize * 0.26f),
            style = Stroke(width = stroke),
        )

        // Folded corner core accent
        rotate(degrees = angle * 0.25f, pivot = Offset(w / 2f, h / 2f)) {
            val foldPath = Path().apply {
                val r = boxSize * 0.30f
                val cx = w / 2f
                val cy = h / 2f
                moveTo(cx - r, cy + r * 0.15f)
                lineTo(cx - r, cy - r)
                lineTo(cx + r * 0.15f, cy - r)
                moveTo(cx + r, cy - r * 0.15f)
                lineTo(cx + r, cy + r)
                lineTo(cx - r * 0.15f, cy + r)
            }
            drawPath(
                path = foldPath,
                color = color.copy(alpha = 0.92f),
                style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }

        // Calm center dot
        drawCircle(
            color = color.copy(alpha = 0.55f + 0.45f * breathe),
            radius = stroke * 0.85f,
            center = Offset(w / 2f, h / 2f),
        )
    }
}

/**
 * Anti-flash loading visibility envelope:
 * 140ms delay -> 160ms fade-in -> 240ms minimum dwell -> 180ms fade-out.
 * Prevents distracting loader flickers on fast cache or local-network responses.
 */
@Composable
fun rememberIgngLoadingVisibility(loading: Boolean): State<Boolean> {
    val visible = remember { mutableStateOf(false) }
    var shownAtMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(loading) {
        if (loading) {
            delay(140L)
            shownAtMs = System.currentTimeMillis()
            visible.value = true
        } else if (visible.value) {
            val elapsed = System.currentTimeMillis() - shownAtMs
            val remaining = (240L - elapsed).coerceAtLeast(0L)
            if (remaining > 0L) delay(remaining)
            visible.value = false
        }
    }
    return visible
}

@Composable
fun IgngLoadingCenter(
    loading: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
) {
    val visible by rememberIgngLoadingVisibility(loading)
    val intensity = IgngTheme.motionIntensity
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(IgngMotion.easeTween(160, intensity)),
        exit = fadeOut(IgngMotion.easeTween(180, intensity)),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            IgngFoldedCoreGlyph(size = size)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IgngPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
        modifier = modifier,
        indicator = {
            if (isRefreshing || state.distanceFraction > 0.1f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp),
                ) {
                    IgngFoldedCoreGlyph(size = 26.dp)
                }
            }
        },
        content = content,
    )
}
