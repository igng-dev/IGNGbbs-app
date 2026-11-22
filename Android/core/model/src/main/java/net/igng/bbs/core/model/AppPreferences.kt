package net.igng.bbs.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

@Serializable
enum class MotionIntensity {
    FULL,
    CALM,
    REDUCED,
}

@Serializable
enum class ReaderCanvasTone {
    DEFAULT,
    WARM_PAPER,
    INK_DARK,
}

@Serializable
data class ReaderTypographyPrefs(
    val fontSizeSp: Float = 16.5f,
    val lineHeightMultiplier: Float = 1.72f,
    val canvasTone: ReaderCanvasTone = ReaderCanvasTone.DEFAULT,
    val preferSerif: Boolean = false,
)

@Serializable
data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val motionIntensity: MotionIntensity = MotionIntensity.FULL,
    val hapticsEnabled: Boolean = true,
    val collapseSeriesInFeed: Boolean = true,
    val backgroundNotificationsEnabled: Boolean = true,
    val reader: ReaderTypographyPrefs = ReaderTypographyPrefs(),
)
