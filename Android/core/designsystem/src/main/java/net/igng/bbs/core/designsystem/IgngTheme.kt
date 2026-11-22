package net.igng.bbs.core.designsystem

import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.igng.bbs.core.model.MotionIntensity
import net.igng.bbs.core.model.ReaderCanvasTone
import net.igng.bbs.core.model.ThemeMode

@Immutable
data class IgngColors(
    val isDark: Boolean,
    val canvas: Color,
    val tile: Color,
    val tileSoft: Color,
    val tilePressed: Color,
    val line: Color,
    val lineStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val accentHover: Color,
    val accentLow: Color,
    val onAccent: Color,
    val success: Color,
    val successLow: Color,
    val warning: Color,
    val warningLow: Color,
    val danger: Color,
    val dangerLow: Color,
    val glassBar: Color,
    val codeBackground: Color,
    val quoteBar: Color,
    val warmPaperCanvas: Color,
    val warmPaperText: Color,
    val warmPaperSecondary: Color,
)

val IgngLightColors = IgngColors(
    isDark = false,
    canvas = Color(0xFFF4F6FA),
    tile = Color(0xFFFFFFFF),
    tileSoft = Color(0xFFF7F9FC),
    tilePressed = Color(0xFFF0F4FA),
    line = Color(0x170F172A),
    lineStrong = Color(0x2E0F172A),
    textPrimary = Color(0xFF1F2328),
    textSecondary = Color(0xFF57606A),
    textMuted = Color(0xFF8C959F),
    accent = Color(0xFF2563EB),
    accentHover = Color(0xFF1D4ED8),
    accentLow = Color(0x142563EB),
    onAccent = Color(0xFFFFFFFF),
    success = Color(0xFF116329),
    successLow = Color(0x14116329),
    warning = Color(0xFF9A6700),
    warningLow = Color(0x149A6700),
    danger = Color(0xFFB42318),
    dangerLow = Color(0x14B42318),
    glassBar = Color(0xEBFFFFFF),
    codeBackground = Color(0xFFF1F4F9),
    quoteBar = Color(0xFFCBD5E1),
    warmPaperCanvas = Color(0xFFFAF6EE),
    warmPaperText = Color(0xFF2C261F),
    warmPaperSecondary = Color(0xFF6B6053),
)

val IgngDarkColors = IgngColors(
    isDark = true,
    canvas = Color(0xFF0B0F16),
    tile = Color(0xFF141A23),
    tileSoft = Color(0xFF19202B),
    tilePressed = Color(0xFF1E2735),
    line = Color(0x17FFFFFF),
    lineStrong = Color(0x30FFFFFF),
    textPrimary = Color(0xFFF0F6FC),
    textSecondary = Color(0xFF8B949E),
    textMuted = Color(0xFF6E7681),
    accent = Color(0xFF58A6FF),
    accentHover = Color(0xFF79B8FF),
    accentLow = Color(0x1F58A6FF),
    onAccent = Color(0xFF0D1117),
    success = Color(0xFF56D364),
    successLow = Color(0x1F56D364),
    warning = Color(0xFFE3B341),
    warningLow = Color(0x1FE3B341),
    danger = Color(0xFFFF7B72),
    dangerLow = Color(0x1FFF7B72),
    glassBar = Color(0xE8141A23),
    codeBackground = Color(0xFF101620),
    quoteBar = Color(0xFF334155),
    warmPaperCanvas = Color(0xFF171410),
    warmPaperText = Color(0xFFEAE2D3),
    warmPaperSecondary = Color(0xFFA89C89),
)

object IgngRadius {
    val tile = 20.dp
    val card = 16.dp
    val inner = 14.dp
    val control = 12.dp
    val pill = 999.dp
}

val LocalIgngColors = staticCompositionLocalOf { IgngLightColors }
val LocalMotionIntensity = staticCompositionLocalOf { MotionIntensity.FULL }
val LocalHapticsEnabled = staticCompositionLocalOf { true }

object IgngTheme {
    val colors: IgngColors
        @Composable get() = LocalIgngColors.current

    val motionIntensity: MotionIntensity
        @Composable get() = LocalMotionIntensity.current

    val hapticsEnabled: Boolean
        @Composable get() = LocalHapticsEnabled.current
}

fun IgngColors.readerSurfaceColors(tone: ReaderCanvasTone): Triple<Color, Color, Color> = when (tone) {
    ReaderCanvasTone.WARM_PAPER -> Triple(warmPaperCanvas, warmPaperText, warmPaperSecondary)
    ReaderCanvasTone.INK_DARK -> Triple(Color(0xFF0D1117), Color(0xFFE6EDF3), Color(0xFF8B949E))
    ReaderCanvasTone.DEFAULT -> Triple(canvas, textPrimary, textSecondary)
}

private val IgngTypography = Typography(
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 25.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.5.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.5.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)

private val IgngShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun IgngBbsTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    motionIntensity: MotionIntensity = MotionIntensity.FULL,
    hapticsEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val effectiveMotion = remember(motionIntensity, context) {
        val sysScale = runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        }.getOrDefault(1f)
        if (sysScale == 0f) MotionIntensity.REDUCED else motionIntensity
    }

    val colors = if (dark) IgngDarkColors else IgngLightColors
    val m3Scheme = if (dark) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            primaryContainer = colors.accentLow,
            onPrimaryContainer = colors.accent,
            background = colors.canvas,
            onBackground = colors.textPrimary,
            surface = colors.tile,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.tileSoft,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.lineStrong,
            outlineVariant = colors.line,
            error = colors.danger,
            onError = colors.onAccent,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = colors.onAccent,
            primaryContainer = colors.accentLow,
            onPrimaryContainer = colors.accent,
            background = colors.canvas,
            onBackground = colors.textPrimary,
            surface = colors.tile,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.tileSoft,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.lineStrong,
            outlineVariant = colors.line,
            error = colors.danger,
            onError = colors.onAccent,
        )
    }

    CompositionLocalProvider(
        LocalIgngColors provides colors,
        LocalMotionIntensity provides effectiveMotion,
        LocalHapticsEnabled provides hapticsEnabled,
    ) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography = IgngTypography,
            shapes = IgngShapes,
            content = content,
        )
    }
}
