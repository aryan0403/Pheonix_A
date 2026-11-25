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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.analytics.GeneratedInsight
import com.example.analytics.InsightChartData
import com.example.data.DatasetSpec
import com.example.ui.components.BadgePill
import com.example.ui.components.BarDatum
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowPrimaryButton
import com.example.ui.components.MetricBarChart
import com.example.ui.components.ScatterDatum
import com.example.ui.components.ScatterPlotChart
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.Navy950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletGlow

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsightGeneratorScreen(
    dataset: DatasetSpec,
    insights: List<GeneratedInsight>,
    generatedTimestamp: String?,
    onGenerateInsights: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Control Banner
        item {
            GlassCard(borderColor = CyanGlow.copy(alpha = 0.28f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "AUTOMATED DISCOVERY ENGINE",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyanGlow
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Insight Generator — ${dataset.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Slate100
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Run live statistical scans across correlations, subgroup gaps, and IQR outliers to surface 3 evidence-backed findings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(Modifier.height(14.dp))
                GlowPrimaryButton(
                    text = stringResource(R.string.cta_generate_insights),
                    onClick = onGenerateInsights,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_insights_button"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = Navy950,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        if (insights.isEmpty()) {
            item {
                GlassCard(
                    contentPadding = PaddingValues(28.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CyanGlow.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.35f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = CyanGlow,
                                modifier = Modifier
                                    .padding(14.dp)
                                    .size(28.dp)
                            )
                        }
                        Text(
                            text = "Ready to Scan ${dataset.records.size} Records",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate100
                        )
                        Text(
                            text = "Tap GENERATE HIDDEN INSIGHTS above to extract three verified discoveries with charts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "3 verified findings for ${dataset.shortName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                    if (generatedTimestamp != null) {
                        Text(
                            text = "Scanned $generatedTimestamp",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanGlow
                        )
                    }
                }
            }

            itemsIndexed(insights, key = { _, ins -> ins.id }) { idx, insight ->
                GlassCard(borderColor = CyanGlow.copy(alpha = 0.25f)) {
                    BadgePill(
                        text = "INSIGHT #${idx + 1} • ${insight.badge}",
                        accentColor = if (idx == 2) AmberGlow else if (idx == 1) VioletGlow else CyanGlow
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = insight.headline,
                        style = MaterialTheme.typography.titleLarge,
                        color = Slate100
                    )
                    Spacer(Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        insight.evidence.forEach { ev ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Navy950.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, Slate400.copy(alpha = 0.22f))
                            ) {
                                Text(
                                    text = ev,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyanGlow,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = insight.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate300
                    )
                    Spacer(Modifier.height(14.dp))

                    when (val chart = insight.chart) {
                        is InsightChartData.ScatterData -> {
                            ScatterPlotChart(
                                points = chart.points.mapIndexed { i, p ->
                                    ScatterDatum("Point ${i + 1}", p.first, p.second)
                                },
                                xLabel = chart.xLabel,
                                yLabel = chart.yLabel,
                                pointColor = CyanGlow,
                                height = 165.dp
                            )
                        }

                        is InsightChartData.BarData -> {
                            MetricBarChart(
                                bars = chart.bars.map {
                                    BarDatum(it.label, it.label, it.value, it.isOutlier)
                                },
                                primaryColor = if (idx == 1) VioletGlow else ElectricBlue,
                                outlierColor = AmberGlow,
                                height = 165.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
