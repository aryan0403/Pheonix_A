package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.analytics.AnalyticsEngine
import com.example.data.TrendPoint
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.Navy950
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletGlow
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

data class BarDatum(
    val label: String,
    val fullName: String,
    val value: Double,
    val isOutlier: Boolean = false
)

data class ScatterDatum(
    val label: String,
    val x: Double,
    val y: Double
)

@Composable
fun AreaTrendChart(
    data: List<TrendPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 170.dp,
    primaryColor: Color = CyanGlow,
    benchmarkColor: Color = VioletGlow
) {
    if (data.isEmpty()) return
    var selectedIdx by remember(data) { mutableStateOf<Int?>(null) }

    val allVals = data.flatMap { listOf(it.value, it.benchmark) }
    val rawMin = allVals.minOrNull() ?: 0.0
    val rawMax = allVals.maxOrNull() ?: 100.0
    val pad = max(1.0, (rawMax - rawMin) * 0.15)
    val minY = rawMin - pad
    val maxY = rawMax + pad
    val rangeY = max(1e-6, maxY - minY)

    Column(modifier = modifier) {
        selectedIdx?.let { idx ->
            data.getOrNull(idx)?.let { pt ->
                Text(
                    text = "${pt.period}: Observed ${AnalyticsEngine.formatNum(pt.value, 1)}  •  Baseline ${AnalyticsEngine.formatNum(pt.benchmark, 1)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(14.dp))
                .background(Navy950.copy(alpha = 0.55f))
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            if (data.size > 1) {
                                val stepX = size.width.toFloat() / (data.size - 1)
                                val idx = ((offset.x / stepX) + 0.5f).toInt().coerceIn(0, data.lastIndex)
                                selectedIdx = idx
                            }
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val gridLines = 3
                for (g in 0..gridLines) {
                    val y = h * g / gridLines
                    drawLine(
                        color = Slate400.copy(alpha = 0.16f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val stepX = if (data.size > 1) w / (data.size - 1) else w
                val obsPoints = data.mapIndexed { i, pt ->
                    val x = i * stepX
                    val y = h - (((pt.value - minY) / rangeY).toFloat() * h)
                    Offset(x, y)
                }
                val benchPoints = data.mapIndexed { i, pt ->
                    val x = i * stepX
                    val y = h - (((pt.benchmark - minY) / rangeY).toFloat() * h)
                    Offset(x, y)
                }

                val areaPath = Path().apply {
                    moveTo(obsPoints.first().x, h)
                    obsPoints.forEach { lineTo(it.x, it.y) }
                    lineTo(obsPoints.last().x, h)
                    close()
                }
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor.copy(alpha = 0.38f), Color.Transparent)
                    )
                )

                val benchPath = Path().apply {
                    benchPoints.forEachIndexed { idx, p ->
                        if (idx == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                    }
                }
                drawPath(
                    path = benchPath,
                    color = benchmarkColor.copy(alpha = 0.85f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                    )
                )

                val obsPath = Path().apply {
                    obsPoints.forEachIndexed { idx, p ->
                        if (idx == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                    }
                }
                drawPath(
                    path = obsPath,
                    color = primaryColor,
                    style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
                )

                obsPoints.forEachIndexed { idx, p ->
                    val isSel = selectedIdx == idx
                    drawCircle(
                        color = if (isSel) Color.White else primaryColor,
                        radius = if (isSel) 5.5.dp.toPx() else 3.5.dp.toPx(),
                        center = p
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach { pt ->
                Text(
                    text = pt.period,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }
        }
    }
}

@Composable
fun MetricBarChart(
    bars: List<BarDatum>,
    unit: String = "",
    primaryColor: Color = CyanGlow,
    outlierColor: Color = AmberGlow,
    upperFence: Double? = null,
    lowerFence: Double? = null,
    height: Dp = 200.dp,
    modifier: Modifier = Modifier
) {
    if (bars.isEmpty()) return
    var selectedIdx by remember(bars) { mutableStateOf<Int?>(null) }

    val allVals = bars.map { it.value } + listOfNotNull(upperFence, lowerFence)
    val maxVal = max(1.0, (allVals.maxOrNull() ?: 10.0) * 1.1)
    val minVal = min(0.0, allVals.minOrNull() ?: 0.0)
    val range = max(1e-6, maxVal - minVal)

    Column(modifier = modifier) {
        selectedIdx?.let { idx ->
            bars.getOrNull(idx)?.let { b ->
                Text(
                    text = "${b.fullName}: ${AnalyticsEngine.formatNum(b.value, 1)}$unit${if (b.isOutlier) "  [OUTLIER]" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (b.isOutlier) outlierColor else primaryColor,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(14.dp))
                .background(Navy950.copy(alpha = 0.55f))
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(bars) {
                        detectTapGestures { offset ->
                            val slotW = size.width.toFloat() / bars.size
                            val idx = (offset.x / slotW).toInt().coerceIn(0, bars.lastIndex)
                            selectedIdx = idx
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val slotW = w / bars.size
                val barW = max(4.dp.toPx(), slotW * 0.58f)

                for (g in 0..3) {
                    val y = h * g / 3f
                    drawLine(
                        color = Slate400.copy(alpha = 0.14f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                bars.forEachIndexed { idx, datum ->
                    val norm = ((datum.value - minVal) / range).toFloat().coerceIn(0.03f, 1f)
                    val barH = norm * h
                    val left = idx * slotW + (slotW - barW) / 2f
                    val top = h - barH
                    val color = if (datum.isOutlier) outlierColor else primaryColor
                    drawRoundRect(
                        color = if (selectedIdx == idx) color else color.copy(alpha = 0.85f),
                        topLeft = Offset(left, top),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }

                upperFence?.let { uf ->
                    val y = h - (((uf - minVal) / range).toFloat() * h)
                    if (y in 0f..h) {
                        drawLine(
                            color = outlierColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.6.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                        )
                    }
                }
                lowerFence?.let { lf ->
                    if (lf > 0.0) {
                        val y = h - (((lf - minVal) / range).toFloat() * h)
                        if (y in 0f..h) {
                            drawLine(
                                color = outlierColor,
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.6.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                            )
                        }
                    }
                }
            }
        }

        if (bars.size <= 12) {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bars.forEach { b ->
                    Text(
                        text = b.label.take(6),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (b.isOutlier) outlierColor else Slate400,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ScatterPlotChart(
    points: List<ScatterDatum>,
    xLabel: String,
    yLabel: String,
    pointColor: Color = CyanGlow,
    highlightPoint: ScatterDatum? = null,
    highlightColor: Color = EmeraldGlow,
    referenceY: Double? = null,
    height: Dp = 210.dp,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    var selectedPoint by remember(points, highlightPoint) { mutableStateOf<ScatterDatum?>(null) }

    val allPts = if (highlightPoint != null) points + highlightPoint else points
    val minX = allPts.minOf { it.x }
    val maxX = allPts.maxOf { it.x }
    val padX = max(0.5, (maxX - minX) * 0.12)
    val loX = minX - padX
    val hiX = maxX + padX
    val spanX = max(1e-6, hiX - loX)

    val allY = allPts.map { it.y } + listOfNotNull(referenceY)
    val minY = allY.minOrNull() ?: 0.0
    val maxY = allY.maxOrNull() ?: 100.0
    val padY = max(1.0, (maxY - minY) * 0.14)
    val loY = minY - padY
    val hiY = maxY + padY
    val spanY = max(1e-6, hiY - loY)

    Column(modifier = modifier) {
        selectedPoint?.let { pt ->
            Text(
                text = "${pt.label}: $xLabel=${AnalyticsEngine.formatNum(pt.x, 1)}, $yLabel=${AnalyticsEngine.formatNum(pt.y, 1)}",
                style = MaterialTheme.typography.labelSmall,
                color = Slate200,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(14.dp))
                .background(Navy950.copy(alpha = 0.55f))
                .padding(14.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(allPts) {
                        detectTapGestures { tap ->
                            val w = size.width.toFloat()
                            val h = size.height.toFloat()
                            val nearest = allPts.minByOrNull { pt ->
                                val px = (((pt.x - loX) / spanX).toFloat() * w)
                                val py = h - (((pt.y - loY) / spanY).toFloat() * h)
                                hypot((tap.x - px).toDouble(), (tap.y - py).toDouble())
                            }
                            selectedPoint = nearest
                        }
                    }
            ) {
                val w = size.width
                val h = size.height

                for (g in 0..3) {
                    val y = h * g / 3f
                    drawLine(
                        color = Slate400.copy(alpha = 0.14f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    val x = w * g / 3f
                    drawLine(
                        color = Slate400.copy(alpha = 0.10f),
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                referenceY?.let { ry ->
                    val py = h - (((ry - loY) / spanY).toFloat() * h)
                    if (py in 0f..h) {
                        drawLine(
                            color = Slate400.copy(alpha = 0.7f),
                            start = Offset(0f, py),
                            end = Offset(w, py),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                        )
                    }
                }

                points.forEach { pt ->
                    val px = (((pt.x - loX) / spanX).toFloat() * w)
                    val py = h - (((pt.y - loY) / spanY).toFloat() * h)
                    val isSel = selectedPoint == pt
                    drawCircle(
                        color = pointColor.copy(alpha = if (isSel) 1f else 0.78f),
                        radius = if (isSel) 6.5.dp.toPx() else 4.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                highlightPoint?.let { hp ->
                    val px = (((hp.x - loX) / spanX).toFloat() * w)
                    val py = h - (((hp.y - loY) / spanY).toFloat() * h)
                    drawCircle(
                        color = highlightColor.copy(alpha = 0.25f),
                        radius = 12.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = highlightColor,
                        radius = 6.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "X: $xLabel (${AnalyticsEngine.formatNum(minX, 1)}–${AnalyticsEngine.formatNum(maxX, 1)})",
                style = MaterialTheme.typography.labelSmall,
                color = Slate400
            )
            Text(
                text = "Y: $yLabel",
                style = MaterialTheme.typography.labelSmall,
                color = Slate400
            )
        }
    }
}
