package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.analytics.AnalyticsEngine
import com.example.data.DatasetSpec
import com.example.ui.components.GlassCard
import com.example.ui.components.ScatterDatum
import com.example.ui.components.ScatterPlotChart
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletGlow
import kotlin.math.max
import kotlin.math.min

@Composable
fun PredictionLabScreen(
    dataset: DatasetSpec,
    modifier: Modifier = Modifier
) {
    val targetMeta = remember(dataset.id) { dataset.metricByKey(dataset.primaryTarget) }
    val predictorMetas = remember(dataset.id) {
        dataset.predictors.map { dataset.metricByKey(it) }
    }
    val model = remember(dataset.id) {
        AnalyticsEngine.fitMultipleRegression(dataset)
    }

    val inputs = remember(dataset.id) {
        mutableStateMapOf<String, Double>().apply {
            predictorMetas.forEach { m ->
                put(m.key, AnalyticsEngine.roundVal(AnalyticsEngine.mean(dataset.records.map { it.getValue(m.key) }), 1))
            }
        }
    }

    val rawPrediction = model.predict(inputs)
    val clampedPrediction = AnalyticsEngine.roundVal(
        min(targetMeta.max * 1.1, max(targetMeta.min, rawPrediction)),
        1
    )
    val cohortAvg = remember(dataset.id) {
        AnalyticsEngine.roundVal(
            AnalyticsEngine.mean(dataset.records.map { it.getValue(targetMeta.key) }),
            1
        )
    }
    val deltaVsAvg = AnalyticsEngine.roundVal(clampedPrediction - cohortAvg, 1)

    val primaryX = predictorMetas.first()
    val actualPoints = remember(dataset.id) {
        dataset.records.map { r ->
            ScatterDatum(
                label = r.name,
                x = r.getValue(primaryX.key),
                y = r.getValue(targetMeta.key)
            )
        }
    }
    val simulatedPoint = ScatterDatum(
        label = "Your Simulation",
        x = inputs[primaryX.key] ?: primaryX.min,
        y = clampedPrediction
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header + Regression Metrics
        item {
            GlassCard(borderColor = EmeraldGlow.copy(alpha = 0.28f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = EmeraldGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "PREDICTION LAB (MULTIPLE LINEAR REGRESSION)",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldGlow
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Simulate ${targetMeta.label}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Slate100
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Adjust sliders below to see how an OLS model fitted on ${dataset.records.size} records updates its estimate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModelMetricBox("Model R²", "${model.r2}", EmeraldGlow, Modifier.weight(1f))
                    ModelMetricBox("RMSE", "±${model.rmse}${targetMeta.unit}", CyanGlow, Modifier.weight(1f))
                    ModelMetricBox("MAE", "±${model.mae}${targetMeta.unit}", VioletGlow, Modifier.weight(1f))
                }
            }
        }

        // Predicted Outcome Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    borderColor = CyanGlow.copy(alpha = 0.4f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text(
                        text = "PREDICTED ${targetMeta.label.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanGlow
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "$clampedPrediction${targetMeta.unit}",
                        style = MaterialTheme.typography.displaySmall,
                        color = Slate100
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Error band: ±${model.rmse}${targetMeta.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate300
                    )
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text(
                        text = "VS COHORT AVG",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${if (deltaVsAvg >= 0) "+" else ""}$deltaVsAvg${targetMeta.unit}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (deltaVsAvg >= 0) EmeraldGlow else AmberGlow
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Avg: $cohortAvg${targetMeta.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }
        }

        // Sliders Card
        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = null,
                        tint = CyanGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Input Variables",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate100
                    )
                }
                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    predictorMetas.forEach { m ->
                        val currentVal = inputs[m.key] ?: m.min
                        val coeff = model.coefficients[m.key] ?: 0.0
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Navy950.copy(alpha = 0.72f),
                            border = BorderStroke(1.dp, Slate400.copy(alpha = 0.16f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = m.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Slate200
                                    )
                                    Text(
                                        text = "${AnalyticsEngine.formatNum(currentVal, 1)}${m.unit}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = CyanGlow
                                    )
                                }
                                Slider(
                                    value = currentVal.toFloat(),
                                    onValueChange = { raw ->
                                        val snapped = AnalyticsEngine.roundVal(
                                            (kotlin.math.round(raw / m.step) * m.step),
                                            1
                                        ).coerceIn(m.min, m.max)
                                        inputs[m.key] = snapped
                                    },
                                    valueRange = m.min.toFloat()..m.max.toFloat(),
                                    colors = SliderDefaults.colors(
                                        thumbColor = CyanGlow,
                                        activeTrackColor = CyanGlow,
                                        inactiveTrackColor = Navy800
                                    ),
                                    modifier = Modifier.testTag("slider_${m.key}")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Range: ${m.min}–${m.max}${m.unit}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400
                                    )
                                    Text(
                                        text = "Slope: ${if (coeff >= 0) "+" else ""}$coeff",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VioletGlow
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AmberGlow.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, AmberGlow.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = AmberGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.prediction_estimate_notice),
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate200
                        )
                    }
                }
            }
        }

        // Scatter Comparison Chart
        item {
            GlassCard {
                Text(
                    text = "Simulated Point vs. Historical Cohort",
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate100
                )
                Text(
                    text = "Blue: Actual Records • Emerald: Your Simulation • Dashed: Cohort Avg",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
                Spacer(Modifier.height(12.dp))
                ScatterPlotChart(
                    points = actualPoints,
                    xLabel = primaryX.label,
                    yLabel = targetMeta.label,
                    pointColor = ElectricBlue,
                    highlightPoint = simulatedPoint,
                    highlightColor = EmeraldGlow,
                    referenceY = cohortAvg,
                    height = 210.dp
                )
            }
        }
    }
}

@Composable
private fun ModelMetricBox(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Navy950.copy(alpha = 0.8f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = Slate400)
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = color)
        }
    }
}
