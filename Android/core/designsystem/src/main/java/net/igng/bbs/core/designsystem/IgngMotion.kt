package net.igng.bbs.core.designsystem

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.igng.bbs.core.model.MotionIntensity

/**
 * IGNG Bento 2.0 comfort-oriented motion grammar.
 * Every curve prioritizes gentle eye-tracking, tactile clarity, and zero visual fatigue:
 * fast responsive launch with long silky deceleration, never bouncy or distracting.
 */
object IgngMotion {
    val Ease = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val LoadEase = CubicBezierEasing(0.33f, 0f, 0.2f, 1f)

    const val QuickMs = 150
    const val StandardMs = 240
    const val SpatialMs = 340
    const val StaggerStepMs = 36
    const val MaxStaggerIndex = 5

    fun <T> quickSpring(intensity: MotionIntensity = MotionIntensity.FULL): FiniteAnimationSpec<T> = when (intensity) {
        MotionIntensity.REDUCED -> snap()
        MotionIntensity.CALM -> tween(QuickMs, easing = Ease)
        MotionIntensity.FULL -> spring(dampingRatio = 0.86f, stiffness = 620f)
    }

    fun <T> standardSpring(intensity: MotionIntensity = MotionIntensity.FULL): FiniteAnimationSpec<T> = when (intensity) {
        MotionIntensity.REDUCED -> snap()
        MotionIntensity.CALM -> tween(StandardMs, easing = Ease)
        MotionIntensity.FULL -> spring(dampingRatio = 0.88f, stiffness = 420f)
    }

    fun <T> spatialSpring(intensity: MotionIntensity = MotionIntensity.FULL): FiniteAnimationSpec<T> = when (intensity) {
        MotionIntensity.REDUCED -> snap()
        MotionIntensity.CALM -> tween(SpatialMs, easing = Ease)
        MotionIntensity.FULL -> spring(dampingRatio = 0.90f, stiffness = 360f)
    }

    fun <T> easeTween(durationMs: Int = StandardMs, intensity: MotionIntensity = MotionIntensity.FULL): FiniteAnimationSpec<T> =
        if (intensity == MotionIntensity.REDUCED) snap() else tween(durationMs, easing = Ease)

    fun tabTransition(intensity: MotionIntensity): ContentTransform = when (intensity) {
        MotionIntensity.REDUCED -> fadeIn(snap()) togetherWith fadeOut(snap())
        MotionIntensity.CALM -> fadeIn(tween(180, easing = Ease)) togetherWith fadeOut(tween(140, easing = Ease))
        MotionIntensity.FULL -> (fadeIn(tween(220, easing = Ease)) + scaleIn(tween(240, easing = Ease), initialScale = 0.988f))
            .togetherWith(fadeOut(tween(150, easing = Ease)))
    }

    fun pushEnter(intensity: MotionIntensity) = when (intensity) {
        MotionIntensity.REDUCED -> fadeIn(snap())
        MotionIntensity.CALM -> fadeIn(tween(200, easing = Ease))
        MotionIntensity.FULL -> slideInHorizontally(tween(300, easing = Ease)) { (it * 0.14f).toInt() } +
            fadeIn(tween(240, easing = Ease))
    }

    fun pushExit(intensity: MotionIntensity) = when (intensity) {
        MotionIntensity.REDUCED -> fadeOut(snap())
        MotionIntensity.CALM -> fadeOut(tween(160, easing = Ease))
        MotionIntensity.FULL -> slideOutHorizontally(tween(260, easing = Ease)) { -(it * 0.05f).toInt() } +
            fadeOut(tween(180, easing = Ease))
    }

    fun popEnter(intensity: MotionIntensity) = when (intensity) {
        MotionIntensity.REDUCED -> fadeIn(snap())
        MotionIntensity.CALM -> fadeIn(tween(180, easing = Ease))
        MotionIntensity.FULL -> slideInHorizontally(tween(260, easing = Ease)) { -(it * 0.05f).toInt() } +
            fadeIn(tween(200, easing = Ease))
    }

    fun popExit(intensity: MotionIntensity) = when (intensity) {
        MotionIntensity.REDUCED -> fadeOut(snap())
        MotionIntensity.CALM -> fadeOut(tween(160, easing = Ease))
        MotionIntensity.FULL -> slideOutHorizontally(tween(240, easing = Ease)) { (it * 0.14f).toInt() } +
            fadeOut(tween(180, easing = Ease))
    }
}

/**
 * Tactile scale-on-press modifier with optional subtle haptic feedback.
 * Scales smoothly to 0.985f (no jarring shrinkage) so every card and button feels responsive.
 */
fun Modifier.igngPressScale(
    enabled: Boolean = true,
    pressedScale: Float = 0.985f,
    haptic: Boolean = false,
    role: Role? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val intensity = IgngTheme.motionIntensity
    val hapticsEnabled = IgngTheme.hapticsEnabled
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && intensity != MotionIntensity.REDUCED) pressedScale else 1f,
        animationSpec = IgngMotion.quickSpring(intensity),
        label = "igngPressScale",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            role = role,
            onClick = {
                if (haptic && hapticsEnabled) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onClick()
            },
        )
}

/**
 * Gentle Bento first-paint stagger: only applies to the top 6 visible items (36ms/item, 12dp upward glide).
 * Items below index 5 appear immediately without delay so fast scrolling never feels sluggish.
 */
fun Modifier.igngBentoEnter(index: Int, key: Any? = Unit): Modifier = composed {
    val intensity = IgngTheme.motionIntensity
    if (intensity != MotionIntensity.FULL || index > IgngMotion.MaxStaggerIndex) {
        return@composed this
    }
    val alpha = remember(key) { Animatable(0f) }
    val offsetY = remember(key) { Animatable(22f) }
    LaunchedEffect(key) {
        delay((index.coerceIn(0, IgngMotion.MaxStaggerIndex) * IgngMotion.StaggerStepMs).toLong())
        launch { alpha.animateTo(1f, tween(220, easing = IgngMotion.Ease)) }
        launch { offsetY.animateTo(0f, spring(dampingRatio = 0.88f, stiffness = Spring.StiffnessMediumLow)) }
    }
    this.graphicsLayer {
        this.alpha = alpha.value
        translationY = offsetY.value
    }
}
