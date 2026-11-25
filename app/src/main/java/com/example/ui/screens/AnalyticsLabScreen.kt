package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.analytics.AnalyticsEngine
import com.example.data.DatasetSpec
import com.example.ui.components.AreaTrendChart
import com.example.ui.components.BarDatum
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricBarChart
import com.example.ui.components.ScatterDatum
import com.example.ui.components.ScatterPlotChart
import com.example.ui.components.SelectorDropdown
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.Navy950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletGlow

@Composable
fun AnalyticsLabScreen(
    dataset: DatasetSpec,
    modifier: Modifier = Modifier
) {
    var barMetricKey by remember(dataset.id) { mutableStateOf(dataset.primaryTarget) }
    var xMetricKey by remember(dataset.id) { mutableStateOf(dataset.defaultX) }
    var yMetricKey by remember(dataset.id) { mutableStateOf(dataset.defaultY) }

    val stats = remember(dataset.id) { AnalyticsEngine.computeMetricStats(dataset) }
    val correlations = remember(dataset.id) { AnalyticsEngine.computeCorrelationPairs(dataset) }

    val barMeta = dataset.metricByKey(barMetricKey)
    val xMeta = dataset.metricByKey(xMetricKey)
    val yMeta = dataset.metricByKey(yMetricKey)

    val barData = remember(dataset.id, barMetricKey) {
        dataset.records.take(14).map { r ->
            BarDatum(
                label = r.name.substringBefore(" "),
                fullName = r.name,
                value = r.getValue(barMeta.key)
            )
        }
    }

    val scatterData = remember(dataset.id, xMetricKey, yMetricKey) {
        dataset.records.map { r ->
            ScatterDatum(
                label = r.name,
                x = r.getValue(xMeta.key),
                y = r.getValue(yMeta.key)
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Analytics,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "LIVE DATA ANALYTICS LAB",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyanGlow
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = dataset.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Slate100
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = dataset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Navy950.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("SAMPLE", style = MaterialTheme.typography.labelSmall, color = Slate400)
                            Text(
                                text = "${dataset.records.size} rows",
                                style = MaterialTheme.typography.titleSmall,
                                color = CyanGlow
                            )
                        }
                    }
                }
            }
        }

        // Descriptive Statistical Summary
        item {
            GlassCard {
                Text(
                    text = "Descriptive Statistical Summary",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate100
                )
                Spacer(Modifier.height(12.dp))
                stats.forEachIndexed { idx, s ->
                    val metricSpec = dataset.metricByKey(s.key)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(metricSpec.color)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = s.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = Slate100
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatCell("Mean", "${s.mean}${s.unit}", CyanGlow)
                            StatCell("Median", "${s.median}${s.unit}", Slate200)
                            StatCell("Min–Max", "${s.min}–${s.max}", Slate300)
                            StatCell("Std Dev", "±${s.stdDev}", VioletGlow)
                        }
                        if (idx < stats.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = Slate400.copy(alpha = 0.14f)
                            )
                        }
                    }
                }
            }
        }

        // Individual Bar Chart
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Individual Comparison",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate100
                        )
                        Text(
                            text = "Tap any bar to inspect exact value",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    SelectorDropdown(
                        label = "",
                        selectedText = barMeta.label,
                        items = dataset.metrics,
                        itemLabel = { it.label },
                        onSelect = { barMetricKey = it.key },
                        testTag = "bar_metric_dropdown"
                    )
                }
                Spacer(Modifier.height(12.dp))
                MetricBarChart(
                    bars = barData,
                    unit = barMeta.unit,
                    primaryColor = barMeta.color,
                    height = 190.dp
                )
            }
        }

        // Progression Trend Chart
        item {
            GlassCard {
                Text(
                    text = dataset.trendTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate100
                )
                Text(
                    text = "Cyan: Observed • Violet dashed: Baseline",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(Modifier.height(12.dp))
                AreaTrendChart(
                    data = dataset.trendData,
                    height = 175.dp
                )
            }
        }

        // Interactive Scatter Plot
        item {
            GlassCard {
                Text(
                    text = "Relationship Explorer (Scatter Plot)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate100
                )
                Text(
                    text = "Select X and Y variables and tap dots to inspect",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectorDropdown(
                        label = "X:",
                        selectedText = xMeta.label,
                        items = dataset.metrics,
                        itemLabel = { it.label },
                        onSelect = { xMetricKey = it.key },
                        testTag = "scatter_x_dropdown",
                        accentColor = CyanGlow,
                        modifier = Modifier.weight(1f)
                    )
                    SelectorDropdown(
                        label = "Y:",
                        selectedText = yMeta.label,
                        items = dataset.metrics,
                        itemLabel = { it.label },
                        onSelect = { yMetricKey = it.key },
                        testTag = "scatter_y_dropdown",
                        accentColor = VioletGlow,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                ScatterPlotChart(
                    points = scatterData,
                    xLabel = xMeta.label,
                    yLabel = yMeta.label,
                    pointColor = CyanGlow,
                    height = 200.dp
                )
            }
        }

        // Pearson Correlation Matrix
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CompareArrows,
                        contentDescription = null,
                        tint = VioletGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Correlation Analysis (Pearson r)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate100
                    )
                }
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    correlations.take(6).forEach { c ->
                        val isPos = c.r >= 0
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Navy950.copy(alpha = 0.65f),
                            border = BorderStroke(1.dp, Slate400.copy(alpha = 0.16f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${c.labelA} ↔ ${c.labelB}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate200,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "r = ${c.r}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isPos) CyanGlow else VioletGlow
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(Slate400.copy(alpha = 0.18f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = c.absR.toFloat().coerceIn(0.04f, 1f))
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    if (isPos) listOf(ElectricBlue, CyanGlow)
                                                    else listOf(AmberGlow, VioletGlow)
                                                )
                                            )
                                    )
                                }
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    text = "${c.strength} ${c.direction} correlation",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate400
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricBlue.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.28f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.correlation_causation_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate400)
        Text(text = value, style = MaterialTheme.typography.labelLarge, color = valueColor)
    }
}
