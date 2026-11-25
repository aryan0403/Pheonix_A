package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.analytics.AnalyticsEngine
import com.example.data.DatasetSpec
import com.example.ui.components.BarDatum
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricBarChart
import com.example.ui.components.SelectorDropdown
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.Navy950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnomalyEngineScreen(
    dataset: DatasetSpec,
    modifier: Modifier = Modifier
) {
    var selectedKey by remember(dataset.id) { mutableStateOf(dataset.metrics.first().key) }
    val validMetric = dataset.metricByKey(selectedKey)

    val iqrResult = remember(dataset.id, validMetric.key) {
        AnalyticsEngine.detectAnomaliesIQR(dataset.records, validMetric)
    }
    val allAnomalies = remember(dataset.id) {
        AnalyticsEngine.scanAllAnomalies(dataset)
    }

    val outlierIds = remember(iqrResult) {
        iqrResult.anomalies.map { it.record.id }.toSet()
    }
    val barData = remember(dataset.id, validMetric.key, outlierIds) {
        dataset.records.map { rec ->
            BarDatum(
                label = rec.name.substringBefore(" "),
                fullName = rec.name,
                value = rec.getValue(validMetric.key),
                isOutlier = rec.id in outlierIds
            )
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header & Metric Selector
        item {
            GlassCard(borderColor = AmberGlow.copy(alpha = 0.28f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = AmberGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "ANOMALY DETECTION ENGINE (IQR METHOD)",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberGlow
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Outlier Scanner — ${dataset.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Slate100
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Transparent fences: Lower = Q1 − 1.5×IQR, Upper = Q3 + 1.5×IQR.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(Modifier.height(12.dp))
                SelectorDropdown(
                    label = "Variable:",
                    selectedText = validMetric.label,
                    items = dataset.metrics,
                    itemLabel = { it.label },
                    onSelect = { selectedKey = it.key },
                    testTag = "anomaly_metric_dropdown",
                    accentColor = AmberGlow,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // IQR Fence Telemetry Pills
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FenceChip("Lower Fence", "${iqrResult.lowerFence}${validMetric.unit}", AmberGlow)
                FenceChip("Q1 (25th)", "${iqrResult.q1}${validMetric.unit}", Slate200)
                FenceChip("Median (Q2)", "${iqrResult.median}${validMetric.unit}", CyanGlow)
                FenceChip("Q3 (75th)", "${iqrResult.q3}${validMetric.unit}", Slate200)
                FenceChip("Upper Fence", "${iqrResult.upperFence}${validMetric.unit}", AmberGlow)
            }
        }

        // Distribution Bar Chart with IQR Fences
        item {
            GlassCard {
                Text(
                    text = "${validMetric.label} Distribution with IQR Fences",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate100
                )
                Text(
                    text = "Cyan: Normal Range • Amber: Flagged Outlier • Dashed: 1.5×IQR Fence",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(Modifier.height(12.dp))
                MetricBarChart(
                    bars = barData,
                    unit = validMetric.unit,
                    primaryColor = CyanGlow,
                    outlierColor = AmberGlow,
                    upperFence = iqrResult.upperFence,
                    lowerFence = iqrResult.lowerFence,
                    height = 200.dp
                )
            }
        }

        // Anomaly Cards Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detected Anomalies (${allAnomalies.size} found)",
                    style = MaterialTheme.typography.titleLarge,
                    color = Slate100
                )
                Text(
                    text = "All variables",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate400
                )
            }
        }

        if (allAnomalies.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EmeraldGlow.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGlow,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "No observations fell outside the 1.5 × IQR thresholds in this dataset.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate200
                        )
                    }
                }
            }
        } else {
            items(allAnomalies) { anomaly ->
                GlassCard(borderColor = AmberGlow.copy(alpha = 0.38f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = AmberGlow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = anomaly.direction.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AmberGlow
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${anomaly.record.name} — ${anomaly.metricLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Slate100
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmberGlow.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${anomaly.value}${anomaly.unit}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = AmberGlow,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = anomaly.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate300
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Navy950.copy(alpha = 0.75f)
                    ) {
                        Text(
                            text = "Why investigate? ${anomaly.investigationNote}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CyanGlow.copy(alpha = 0.9f),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FenceChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Navy950.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate400)
            Text(text = value, style = MaterialTheme.typography.titleSmall, color = color)
        }
    }
}
