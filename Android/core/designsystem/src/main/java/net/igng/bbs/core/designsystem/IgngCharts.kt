package net.igng.bbs.core.designsystem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import net.igng.bbs.core.model.BbsFormatters
import net.igng.bbs.core.model.MotionIntensity

data class ChartPoint(
    val label: String,
    val value: Int,
)

/**
 * Smooth cubic Bezier line + soft gradient fill chart for creator 30-day view trends.
 */
@Composable
fun IgngLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    val intensity = IgngTheme.motionIntensity
    if (points.isEmpty()) {
        Text(
            text = "暂无趋势数据",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textMuted,
        )
        return
    }
    val progress = remember(points) { Animatable(if (intensity == MotionIntensity.REDUCED) 1f else 0f) }
    LaunchedEffect(points) {
        if (intensity != MotionIntensity.REDUCED) {
            progress.animateTo(1f, tween(520, easing = IgngMotion.Ease))
        }
    }

    val maxVal = (points.maxOfOrNull { it.value } ?: 1).coerceAtLeast(1)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp),
        ) {
            val w = size.width
            val h = size.height
            val padTop = 14f
            val padBottom = 10f
            val usableH = (h - padTop - padBottom).coerceAtLeast(1f)

            // 3 subtle hairline guide lines
            for (i in 0..2) {
                val y = padTop + usableH * (i / 2f)
                drawLine(
                    color = colors.line,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f,
                )
            }

            val coords = points.mapIndexed { idx, pt ->
                val x = if (points.size == 1) w / 2f else w * idx / (points.size - 1).toFloat()
                val ratio = (pt.value.toFloat() / maxVal.toFloat()) * progress.value
                val y = padTop + usableH * (1f - ratio)
                Offset(x, y)
            }

            if (coords.size >= 2) {
                val strokePath = Path().apply {
                    moveTo(coords.first().x, coords.first().y)
                    for (i in 1 until coords.size) {
                        val prev = coords[i - 1]
                        val curr = coords[i]
                        val midX = (prev.x + curr.x) / 2f
                        cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                    }
                }
                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(coords.last().x, h - padBottom)
                    lineTo(coords.first().x, h - padBottom)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(colors.accent.copy(alpha = 0.22f), colors.accent.copy(alpha = 0.0f)),
                        startY = padTop,
                        endY = h,
                    ),
                )
                drawPath(
                    path = strokePath,
                    color = colors.accent,
                    style = Stroke(width = 4f, cap = StrokeCap.Round),
                )
            }
            coords.forEach { pt ->
                drawCircle(color = colors.tile, radius = 6f, center = pt)
                drawCircle(color = colors.accent, radius = 4f, center = pt)
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = points.first().label.takeLast(5),
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                color = colors.textMuted,
            )
            Text(
                text = "峰值 ${BbsFormatters.compactCount(maxVal)}",
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                color = colors.textSecondary,
            )
            Text(
                text = points.last().label.takeLast(5),
                style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                color = colors.textMuted,
            )
        }
    }
}

@Composable
fun IgngHorizontalBarList(
    items: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
) {
    val colors = IgngTheme.colors
    val maxVal = (items.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items.forEach { (label, value) ->
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                    Text(
                        text = BbsFormatters.compactCount(value),
                        style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
                        color = colors.textPrimary,
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (value.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(IgngRadius.pill)),
                    color = colors.accent,
                    trackColor = colors.line,
                )
            }
        }
    }
}
