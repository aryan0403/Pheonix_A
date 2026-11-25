package com.example

import com.example.analytics.AnalyticsEngine
import com.example.data.DatasetsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsEngineTest {

    @Test
    fun studentDataset_detectsBurnoutAndAttendanceAnomalies() {
        val anomalies = AnalyticsEngine.scanAllAnomalies(DatasetsRepository.studentPerformance)
        val flaggedNames = anomalies.map { it.record.name }.toSet()
        assertTrue(flaggedNames.contains("Aarav J. (Burnout)"))
        assertTrue(flaggedNames.contains("Nikhil Q."))
    }

    @Test
    fun footballDataset_detectsTopScorerOutlier() {
        val anomalies = AnalyticsEngine.scanAllAnomalies(DatasetsRepository.footballAnalytics)
        assertTrue(anomalies.any { it.record.name == "A. Diallo" && it.metricKey == "goals" })
    }

    @Test
    fun healthcareDataset_detectsRecoveryOutliers() {
        val anomalies = AnalyticsEngine.scanAllAnomalies(DatasetsRepository.healthcareResearch)
        val names = anomalies.map { it.record.name }.toSet()
        assertTrue(names.contains("Case #111"))
        assertTrue(names.contains("Case #117"))
    }

    @Test
    fun multipleRegression_achievesHighFitAcrossDatasets() {
        DatasetsRepository.all.forEach { ds ->
            val model = AnalyticsEngine.fitMultipleRegression(ds)
            assertTrue("Expected R^2 > 0.5 for ${ds.name}, got ${model.r2}", model.r2 > 0.5)
            assertTrue("Expected positive RMSE for ${ds.name}", model.rmse > 0.0)
        }
    }

    @Test
    fun insightGenerator_alwaysProducesThreeInsights() {
        DatasetsRepository.all.forEach { ds ->
            val insights = AnalyticsEngine.generateHiddenInsights(ds)
            assertEquals(3, insights.size)
            insights.forEach { ins ->
                assertTrue(ins.headline.isNotBlank())
                assertTrue(ins.evidence.isNotEmpty())
            }
        }
    }
}
