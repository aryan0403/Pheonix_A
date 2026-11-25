package com.example.analytics

import com.example.data.DataRecord
import com.example.data.DatasetSpec
import com.example.data.MetricSpec
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

data class MetricStats(
    val key: String,
    val label: String,
    val unit: String,
    val count: Int,
    val mean: Double,
    val median: Double,
    val min: Double,
    val max: Double,
    val q1: Double,
    val q3: Double,
    val stdDev: Double
)

data class CorrelationPair(
    val keyA: String,
    val labelA: String,
    val keyB: String,
    val labelB: String,
    val r: Double,
    val absR: Double,
    val direction: String,
    val strength: String
)

data class AnomalyRecord(
    val record: DataRecord,
    val metricKey: String,
    val metricLabel: String,
    val unit: String,
    val value: Double,
    val q1: Double,
    val median: Double,
    val q3: Double,
    val iqr: Double,
    val lowerFence: Double,
    val upperFence: Double,
    val direction: String,
    val iqrMultiplier: Double,
    val explanation: String,
    val investigationNote: String
)

data class IqrAnalysisResult(
    val metric: MetricSpec,
    val q1: Double,
    val median: Double,
    val q3: Double,
    val iqr: Double,
    val lowerFence: Double,
    val upperFence: Double,
    val anomalies: List<AnomalyRecord>
)

data class RegressionModel(
    val intercept: Double,
    val coefficients: Map<String, Double>,
    val r2: Double,
    val rmse: Double,
    val mae: Double
) {
    fun predict(inputs: Map<String, Double>): Double {
        var total = intercept
        coefficients.forEach { (key, slope) ->
            total += slope * (inputs[key] ?: 0.0)
        }
        return total
    }
}

sealed interface InsightChartData {
    data class ScatterData(
        val xLabel: String,
        val yLabel: String,
        val points: List<Pair<Double, Double>>
    ) : InsightChartData

    data class BarData(
        val yLabel: String,
        val bars: List<InsightBarEntry>
    ) : InsightChartData
}

data class InsightBarEntry(
    val label: String,
    val value: Double,
    val isOutlier: Boolean = false
)

data class GeneratedInsight(
    val id: String,
    val badge: String,
    val headline: String,
    val evidence: List<String>,
    val explanation: String,
    val chart: InsightChartData
)

object AnalyticsEngine {

    fun roundVal(value: Double, decimals: Int = 2): Double {
        if (value.isNaN() || !value.isFinite()) return 0.0
        val factor = 10.0.pow(decimals)
        return round(value * factor) / factor
    }

    fun formatNum(value: Double, decimals: Int = 1): String {
        val r = roundVal(value, decimals)
        return if (r % 1.0 == 0.0 && decimals == 0) r.toInt().toString() else String.format("%.${decimals}f", r)
    }

    fun mean(values: List<Double>): Double =
        if (values.isEmpty()) 0.0 else values.sum() / values.size

    fun quantile(sorted: List<Double>, q: Double): Double {
        if (sorted.isEmpty()) return 0.0
        val pos = (sorted.size - 1) * q
        val base = floor(pos).toInt()
        val rest = pos - base
        return if (base + 1 < sorted.size) {
            sorted[base] + rest * (sorted[base + 1] - sorted[base])
        } else {
            sorted[base]
        }
    }

    fun stdDev(values: List<Double>): Double {
        if (values.size < 2) return 0.0
        val avg = mean(values)
        val variance = values.sumOf { (it - avg).pow(2) } / (values.size - 1)
        return sqrt(variance)
    }

    fun computeMetricStats(dataset: DatasetSpec): List<MetricStats> =
        dataset.metrics.map { m ->
            val vals = dataset.records.map { it.getValue(m.key) }
            val sorted = vals.sorted()
            MetricStats(
                key = m.key,
                label = m.label,
                unit = m.unit,
                count = vals.size,
                mean = roundVal(mean(vals), 2),
                median = roundVal(quantile(sorted, 0.5), 2),
                min = roundVal(sorted.firstOrNull() ?: 0.0, 2),
                max = roundVal(sorted.lastOrNull() ?: 0.0, 2),
                q1 = roundVal(quantile(sorted, 0.25), 2),
                q3 = roundVal(quantile(sorted, 0.75), 2),
                stdDev = roundVal(stdDev(vals), 2)
            )
        }

    fun pearsonCorrelation(xs: List<Double>, ys: List<Double>): Double {
        if (xs.size != ys.size || xs.size < 2) return 0.0
        val mx = mean(xs)
        val my = mean(ys)
        var num = 0.0
        var dx = 0.0
        var dy = 0.0
        for (i in xs.indices) {
            val vx = xs[i] - mx
            val vy = ys[i] - my
            num += vx * vy
            dx += vx * vx
            dy += vy * vy
        }
        val denom = sqrt(dx * dy)
        return if (denom == 0.0) 0.0 else roundVal(num / denom, 3)
    }

    fun computeCorrelationPairs(dataset: DatasetSpec): List<CorrelationPair> {
        val pairs = mutableListOf<CorrelationPair>()
        val metrics = dataset.metrics
        for (i in metrics.indices) {
            for (j in i + 1 until metrics.size) {
                val mA = metrics[i]
                val mB = metrics[j]
                val r = pearsonCorrelation(
                    dataset.records.map { it.getValue(mA.key) },
                    dataset.records.map { it.getValue(mB.key) }
                )
                val absR = roundVal(abs(r), 3)
                val strength = when {
                    absR >= 0.75 -> "Very Strong"
                    absR >= 0.50 -> "Strong"
                    absR >= 0.30 -> "Moderate"
                    else -> "Weak"
                }
                pairs.add(
                    CorrelationPair(
                        keyA = mA.key,
                        labelA = mA.label,
                        keyB = mB.key,
                        labelB = mB.label,
                        r = r,
                        absR = absR,
                        direction = if (r >= 0) "Positive" else "Negative",
                        strength = strength
                    )
                )
            }
        }
        return pairs.sortedByDescending { it.absR }
    }

    fun detectAnomaliesIQR(records: List<DataRecord>, metric: MetricSpec): IqrAnalysisResult {
        val vals = records.map { it.getValue(metric.key) }
        val sorted = vals.sorted()
        val q1 = quantile(sorted, 0.25)
        val q3 = quantile(sorted, 0.75)
        val med = quantile(sorted, 0.5)
        val iqr = q3 - q1
        val lowerFence = q1 - 1.5 * iqr
        val upperFence = q3 + 1.5 * iqr

        val anomalies = mutableListOf<AnomalyRecord>()
        records.forEach { rec ->
            val v = rec.getValue(metric.key)
            if (v < lowerFence || v > upperFence) {
                val isHigh = v > upperFence
                val mult = if (iqr > 0) abs(v - med) / iqr else 0.0
                val roundedUpper = roundVal(upperFence, 2)
                val roundedLower = roundVal(lowerFence, 2)
                val roundedMed = roundVal(med, 2)
                val roundedMult = roundVal(mult, 1)
                val explanation = if (isHigh) {
                    "${rec.name} recorded $v${metric.unit} in ${metric.label}, exceeding the IQR upper fence ($roundedUpper${metric.unit}) by ${roundVal(v - upperFence, 2)}${metric.unit} (${roundedMult}x IQR above the median of $roundedMed${metric.unit})."
                } else {
                    "${rec.name} recorded $v${metric.unit} in ${metric.label}, falling below the IQR lower fence ($roundedLower${metric.unit}) by ${roundVal(lowerFence - v, 2)}${metric.unit} (${roundedMult}x IQR below the median of $roundedMed${metric.unit})."
                }
                val note = if (isHigh) {
                    "Investigate whether this reflects exceptional performance, extreme workload/dosage, or a special subgroup effect."
                } else {
                    "Investigate potential external barriers, data entry anomalies, or risk factors driving unusually low values."
                }
                anomalies.add(
                    AnomalyRecord(
                        record = rec,
                        metricKey = metric.key,
                        metricLabel = metric.label,
                        unit = metric.unit,
                        value = roundVal(v, 2),
                        q1 = roundVal(q1, 2),
                        median = roundedMed,
                        q3 = roundVal(q3, 2),
                        iqr = roundVal(iqr, 2),
                        lowerFence = roundedLower,
                        upperFence = roundedUpper,
                        direction = if (isHigh) "Above Upper Fence" else "Below Lower Fence",
                        iqrMultiplier = roundedMult,
                        explanation = explanation,
                        investigationNote = note
                    )
                )
            }
        }

        return IqrAnalysisResult(
            metric = metric,
            q1 = roundVal(q1, 2),
            median = roundVal(med, 2),
            q3 = roundVal(q3, 2),
            iqr = roundVal(iqr, 2),
            lowerFence = roundVal(lowerFence, 2),
            upperFence = roundVal(upperFence, 2),
            anomalies = anomalies
        )
    }

    fun scanAllAnomalies(dataset: DatasetSpec): List<AnomalyRecord> =
        dataset.metrics.flatMap { detectAnomaliesIQR(dataset.records, it).anomalies }

    private fun solveLinearSystem(aIn: Array<DoubleArray>, bIn: DoubleArray): DoubleArray {
        val n = aIn.size
        val aug = Array(n) { r ->
            DoubleArray(n + 1) { c -> if (c < n) aIn[r][c] else bIn[r] }
        }
        for (col in 0 until n) {
            var pivotRow = col
            for (r in col + 1 until n) {
                if (abs(aug[r][col]) > abs(aug[pivotRow][col])) {
                    pivotRow = r
                }
            }
            val tmp = aug[col]
            aug[col] = aug[pivotRow]
            aug[pivotRow] = tmp

            val pivot = if (abs(aug[col][col]) < 1e-10) 1e-8 else aug[col][col]
            for (j in col..n) {
                aug[col][j] /= pivot
            }
            for (r in 0 until n) {
                if (r != col) {
                    val factor = aug[r][col]
                    for (j in col..n) {
                        aug[r][j] -= factor * aug[col][j]
                    }
                }
            }
        }
        return DoubleArray(n) { r -> aug[r][n] }
    }

    fun fitMultipleRegression(dataset: DatasetSpec): RegressionModel {
        val featureKeys = dataset.predictors
        val targetKey = dataset.primaryTarget
        val records = dataset.records
        val n = records.size
        val p = featureKeys.size + 1

        val xMat = Array(n) { i ->
            DoubleArray(p) { j -> if (j == 0) 1.0 else records[i].getValue(featureKeys[j - 1]) }
        }
        val y = DoubleArray(n) { i -> records[i].getValue(targetKey) }

        val xtX = Array(p) { DoubleArray(p) }
        val xty = DoubleArray(p)

        for (i in 0 until n) {
            for (j in 0 until p) {
                xty[j] += xMat[i][j] * y[i]
                for (k in 0 until p) {
                    xtX[j][k] += xMat[i][j] * xMat[i][k]
                }
            }
        }
        for (j in 1 until p) {
            xtX[j][j] += 1e-4
        }

        val weights = solveLinearSystem(xtX, xty)
        val intercept = weights[0]
        val rawCoeffs = featureKeys.mapIndexed { idx, k -> k to weights[idx + 1] }.toMap()

        val preds = DoubleArray(n) { i ->
            var s = 0.0
            for (j in 0 until p) s += xMat[i][j] * weights[j]
            s
        }
        val meanY = y.average()
        var ssRes = 0.0
        var ssTot = 0.0
        var absErrSum = 0.0
        for (i in 0 until n) {
            val err = y[i] - preds[i]
            ssRes += err * err
            absErrSum += abs(err)
            ssTot += (y[i] - meanY).pow(2)
        }
        val r2 = if (ssTot == 0.0) 0.0 else max(0.0, 1.0 - ssRes / ssTot)
        val rmse = sqrt(ssRes / max(1, n))
        val mae = absErrSum / max(1, n)

        return RegressionModel(
            intercept = roundVal(intercept, 3),
            coefficients = rawCoeffs.mapValues { roundVal(it.value, 3) },
            r2 = roundVal(r2, 3),
            rmse = roundVal(rmse, 2),
            mae = roundVal(mae, 2)
        )
    }

    fun generateHiddenInsights(dataset: DatasetSpec): List<GeneratedInsight> {
        val targetMetric = dataset.metricByKey(dataset.primaryTarget)
        val stats = computeMetricStats(dataset)
        val correlations = computeCorrelationPairs(dataset)
        val targetCorrs = correlations.filter {
            it.keyA == targetMetric.key || it.keyB == targetMetric.key
        }
        val topDriver = targetCorrs.firstOrNull() ?: correlations.first()
        val driverKey = if (topDriver.keyA == targetMetric.key) topDriver.keyB else topDriver.keyA
        val driverMetric = dataset.metricByKey(driverKey)
        val targetMean = stats.first { it.key == targetMetric.key }.mean

        val insight1 = GeneratedInsight(
            id = "insight-driver",
            badge = "STRONGEST RELATIONSHIP",
            headline = "${driverMetric.label} Strongly Tracks ${targetMetric.label} (r = ${topDriver.r})",
            evidence = listOf(
                "Pearson r = ${topDriver.r}",
                "${topDriver.strength} ${topDriver.direction}",
                "Mean ${targetMetric.label}: $targetMean${targetMetric.unit}"
            ),
            explanation = "Across all ${dataset.records.size} observations, ${driverMetric.label} shows a ${topDriver.strength.lowercase()} ${topDriver.direction.lowercase()} linear association with ${targetMetric.label}. Correlation reveals a reliable pattern, not automatic cause-and-effect.",
            chart = InsightChartData.ScatterData(
                xLabel = driverMetric.label,
                yLabel = targetMetric.label,
                points = dataset.records.map { it.getValue(driverMetric.key) to it.getValue(targetMetric.key) }
            )
        )

        val groups = dataset.records.groupBy { it.group }.map { (groupName, rows) ->
            groupName to roundVal(mean(rows.map { it.getValue(targetMetric.key) }), 2)
        }.sortedByDescending { it.second }

        val bestGroup = groups.first()
        val lowestGroup = groups.last()
        val gap = roundVal(bestGroup.second - lowestGroup.second, 2)

        val insight2 = GeneratedInsight(
            id = "insight-cohort",
            badge = "GROUP COMPARISON",
            headline = "\"${bestGroup.first}\" Leads Average ${targetMetric.label} by $gap${targetMetric.unit}",
            evidence = listOf(
                "${bestGroup.first}: ${bestGroup.second}${targetMetric.unit}",
                "${lowestGroup.first}: ${lowestGroup.second}${targetMetric.unit}",
                "Spread: $gap${targetMetric.unit}"
            ),
            explanation = "Segmenting the dataset by category uncovers clear group differences. \"${bestGroup.first}\" averages ${bestGroup.second}${targetMetric.unit} compared to ${lowestGroup.second}${targetMetric.unit} for \"${lowestGroup.first}\".",
            chart = InsightChartData.BarData(
                yLabel = targetMetric.label,
                bars = groups.map { InsightBarEntry(it.first, it.second, false) }
            )
        )

        val allAnomalies = scanAllAnomalies(dataset)
        val featured = allAnomalies.firstOrNull()
        val targetStats = stats.first { it.key == targetMetric.key }
        val spread = roundVal(targetStats.max - targetStats.min, 2)

        val insight3 = if (featured != null) {
            val subset = if (dataset.records.take(12).any { it.id == featured.record.id }) {
                dataset.records.take(12)
            } else {
                dataset.records.take(11) + featured.record
            }
            GeneratedInsight(
                id = "insight-anomaly",
                badge = "OUTLIER DISCOVERY",
                headline = "Outlier Spotlight: ${featured.record.name} Deviates ${featured.iqrMultiplier}x IQR",
                evidence = listOf(
                    "${featured.metricLabel}: ${featured.value}${featured.unit}",
                    "Median: ${featured.median}${featured.unit}",
                    "Fences: [${featured.lowerFence}, ${featured.upperFence}]"
                ),
                explanation = "${featured.explanation} Spotting outliers prevents skewed averages and often reveals critical real-world stories hidden in raw tables.",
                chart = InsightChartData.BarData(
                    yLabel = featured.metricLabel,
                    bars = subset.map {
                        InsightBarEntry(
                            label = it.name.substringBefore(" "),
                            value = it.getValue(featured.metricKey),
                            isOutlier = it.id == featured.record.id
                        )
                    }
                )
            )
        } else {
            GeneratedInsight(
                id = "insight-spread",
                badge = "DISTRIBUTION SPREAD",
                headline = "${targetMetric.label} Spans $spread${targetMetric.unit} Across Cohort",
                evidence = listOf(
                    "Min: ${targetStats.min}${targetMetric.unit}",
                    "Max: ${targetStats.max}${targetMetric.unit}",
                    "Std Dev: ±${targetStats.stdDev}${targetMetric.unit}"
                ),
                explanation = "Values vary widely from ${targetStats.min}${targetMetric.unit} to ${targetStats.max}${targetMetric.unit}, showing why median and IQR complement averages.",
                chart = InsightChartData.BarData(
                    yLabel = targetMetric.label,
                    bars = dataset.records.take(12).map {
                        InsightBarEntry(it.name.substringBefore(" "), it.getValue(targetMetric.key), false)
                    }
                )
            )
        }

        return listOf(insight1, insight2, insight3)
    }

    fun buildDatasetDossier(dataset: DatasetSpec): String {
        val stats = computeMetricStats(dataset)
        val corrs = computeCorrelationPairs(dataset)
        val anomalies = scanAllAnomalies(dataset)
        val reg = fitMultipleRegression(dataset)

        return buildString {
            appendLine("Dataset: ${dataset.name} (${dataset.category})")
            appendLine("Description: ${dataset.description}")
            appendLine("Sample size: ${dataset.records.size} records")
            appendLine("\nSummary Statistics:")
            stats.forEach { s ->
                appendLine("- ${s.label}: mean=${s.mean}${s.unit}, median=${s.median}${s.unit}, min=${s.min}${s.unit}, max=${s.max}${s.unit}, Q1=${s.q1}, Q3=${s.q3}, stdDev=${s.stdDev}")
            }
            appendLine("\nPearson Correlations:")
            corrs.forEach { c ->
                appendLine("- ${c.labelA} vs ${c.labelB}: r=${c.r} (${c.strength} ${c.direction})")
            }
            appendLine("\nIQR Anomalies (${anomalies.size} flagged):")
            if (anomalies.isEmpty()) {
                appendLine("- None outside 1.5x IQR fences.")
            } else {
                anomalies.forEach { a ->
                    appendLine("- ${a.record.name} (${a.record.group}) in ${a.metricLabel}: ${a.value}${a.unit} (${a.direction}; median=${a.median}${a.unit}, fence=[${a.lowerFence}, ${a.upperFence}])")
                }
            }
            appendLine("\nMultiple Linear Regression predicting ${dataset.primaryTarget}:")
            appendLine("- R^2=${reg.r2}, RMSE=${reg.rmse}, MAE=${reg.mae}, Intercept=${reg.intercept}")
            reg.coefficients.forEach { (k, slope) ->
                appendLine("- Slope for $k: $slope")
            }
            appendLine("\nRepresentative Rows:")
            dataset.records.take(10).forEach { r ->
                val metricsStr = dataset.metrics.joinToString(", ") { "${it.key}=${r.getValue(it.key)}" }
                appendLine("- ${r.name} [${r.group}]: $metricsStr")
            }
        }
    }

    fun runLocalAnalysis(question: String, dataset: DatasetSpec): String {
        val q = question.lowercase()
        val stats = computeMetricStats(dataset)
        val correlations = computeCorrelationPairs(dataset)
        val anomalies = scanAllAnomalies(dataset)
        val targetMetric = dataset.metricByKey(dataset.primaryTarget)
        val reg = fitMultipleRegression(dataset)

        val targetCorrs = correlations.filter {
            it.keyA == targetMetric.key || it.keyB == targetMetric.key
        }
        val strongest = targetCorrs.firstOrNull() ?: correlations.first()
        val otherKey = if (strongest.keyA == targetMetric.key) strongest.keyB else strongest.keyA
        val otherMetric = dataset.metricByKey(otherKey)

        return when {
            "strongest" in q || "relationship" in q || "correlation" in q -> {
                val lines = targetCorrs.joinToString("\n") { c ->
                    val partnerKey = if (c.keyA == targetMetric.key) c.keyB else c.keyA
                    val partner = dataset.metricByKey(partnerKey)
                    "• **${partner.label}** vs ${targetMetric.label}: **r = ${c.r}** (${c.strength} ${c.direction})"
                }
                """**Strongest Relationship in ${dataset.name}**

Across all **${dataset.records.size} records**, **${otherMetric.label}** has the strongest relationship with **${targetMetric.label}** (**r = ${strongest.r}**, ${strongest.strength} ${strongest.direction}).

**All Correlations with ${targetMetric.label}:**
$lines

**Correlation vs. Causation Note:** A strong correlation (r = ${strongest.r}) means these variables move together consistently in this dataset, **not** that one automatically causes the other."""
            }

            "unusual" in q || "anomal" in q || "outlier" in q -> {
                if (anomalies.isEmpty()) {
                    "Using the 1.5x IQR rule across ${dataset.name}, no extreme univariate outliers fell outside the lower/upper fences."
                } else {
                    val list = anomalies.take(5).joinToString("\n") { a ->
                        "• **${a.record.name}** — ${a.metricLabel}: **${a.value}${a.unit}** (Median: ${a.median}${a.unit}; Fence: [${a.lowerFence}, ${a.upperFence}])"
                    }
                    """**Unusual Observations Detected (IQR Method)**

Scanning **${dataset.records.size} records** using the transparent **1.5 × IQR** rule flagged **${anomalies.size} outlier(s)**:

$list

**Why This Matters:** Outliers can pull averages up or down and often highlight real-world edge cases—such as burnout, elite scoring, or clinical complications."""
                }
            }

            "what would happen" in q || "increas" in q || "study hours" in q || "shots" in q -> {
                val driverKey = dataset.predictors.first()
                val driver = dataset.metricByKey(driverKey)
                val slope = reg.coefficients[driverKey] ?: 0.0
                val delta = when (driverKey) {
                    "shots" -> 10.0
                    "age" -> 5.0
                    else -> 1.0
                }
                val change = roundVal(slope * delta, 2)
                """**Simulated What-If Estimate (${dataset.name})**

Using Multiple Linear Regression (**R² = ${reg.r2}**, **RMSE = ±${reg.rmse}${targetMetric.unit}**):
• Holding other variables constant, each **+${delta.toInt()}${driver.unit}** increase in **${driver.label}** is associated with an estimated change of **${if (change >= 0) "+" else ""}$change${targetMetric.unit}** in **${targetMetric.label}**.

**Statistical Caution:** This is an associational estimate, not a guaranteed causal outcome—for example, extreme study hours without sleep can lead to burnout."""
            }

            "group" in q || "treatment" in q || "position" in q || "compare" in q -> {
                val summary = dataset.records.groupBy { it.group }.entries.joinToString("\n") { (g, rows) ->
                    val avg = roundVal(mean(rows.map { it.getValue(targetMetric.key) }), 2)
                    "• **$g** (n=${rows.size}): Average ${targetMetric.label} = **$avg${targetMetric.unit}**"
                }
                """**Subgroup Comparison in ${dataset.name}**

Comparing all groups on **${targetMetric.label}**:
$summary

Group averages can reflect differences in baseline factors (such as age, minutes played, or severity) alongside group membership."""
            }

            else -> {
                val statLines = stats.joinToString("\n") { s ->
                    "• **${s.label}**: Avg **${s.mean}${s.unit}**, Median **${s.median}${s.unit}** (Range: ${s.min}–${s.max}${s.unit})"
                }
                """**Key Patterns & Conclusions — ${dataset.name} (n = ${dataset.records.size})**

**1. Distribution Summary:**
$statLines

**2. Strongest Pattern:**
• **${otherMetric.label}** and **${targetMetric.label}** show a **${strongest.strength.lowercase()} ${strongest.direction.lowercase()} correlation (r = ${strongest.r})**.
• Predictors explain **${roundVal(reg.r2 * 100.0, 1)}%** of variance (R² = ${reg.r2}).

**3. Outliers Flagged:** **${anomalies.size}** unusual observation(s) detected by the IQR engine."""
            }
        }
    }
}
