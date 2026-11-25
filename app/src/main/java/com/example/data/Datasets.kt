package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.VioletGlow

data class MetricSpec(
    val key: String,
    val label: String,
    val unit: String,
    val color: Color,
    val min: Double,
    val max: Double,
    val step: Double
)

data class TrendPoint(
    val period: String,
    val value: Double,
    val benchmark: Double
)

data class DataRecord(
    val id: String,
    val name: String,
    val group: String,
    val values: Map<String, Double>
) {
    fun getValue(key: String): Double = values[key] ?: 0.0
}

data class DatasetSpec(
    val id: String,
    val name: String,
    val shortName: String,
    val category: String,
    val description: String,
    val primaryTarget: String,
    val defaultX: String,
    val defaultY: String,
    val metrics: List<MetricSpec>,
    val predictors: List<String>,
    val suggestedQuestions: List<String>,
    val trendTitle: String,
    val trendData: List<TrendPoint>,
    val records: List<DataRecord>
) {
    fun metricByKey(key: String): MetricSpec =
        metrics.firstOrNull { it.key == key } ?: metrics.first()
}

object DatasetsRepository {

    val studentPerformance = DatasetSpec(
        id = "student",
        name = "Student Performance",
        shortName = "Students",
        category = "Education Analytics",
        description = "Synthetic academic dataset tracking 24 high school learners across weekly study hours, attendance rate, nightly sleep, and standardized test performance.",
        primaryTarget = "testScore",
        defaultX = "studyHours",
        defaultY = "testScore",
        metrics = listOf(
            MetricSpec("studyHours", "Study Hours / Wk", "hrs", CyanGlow, 1.0, 14.0, 0.5),
            MetricSpec("attendance", "Attendance Rate", "%", ElectricBlue, 45.0, 100.0, 1.0),
            MetricSpec("sleepHours", "Sleep Hours / Night", "hrs", VioletGlow, 3.5, 9.5, 0.2),
            MetricSpec("testScore", "Test Score", "%", EmeraldGlow, 0.0, 100.0, 1.0)
        ),
        predictors = listOf("studyHours", "attendance", "sleepHours"),
        suggestedQuestions = listOf(
            "Which variable has the strongest relationship with performance?",
            "What patterns can you identify?",
            "Are there any unusual observations?",
            "What conclusions can we draw?",
            "What would happen if study hours increased?"
        ),
        trendTitle = "Cohort Average Test Score Progression (Weeks 1–8)",
        trendData = listOf(
            TrendPoint("W1", 68.4, 66.0),
            TrendPoint("W2", 70.1, 67.5),
            TrendPoint("W3", 72.8, 69.0),
            TrendPoint("W4", 71.5, 70.0),
            TrendPoint("W5", 75.2, 71.5),
            TrendPoint("W6", 77.9, 73.0),
            TrendPoint("W7", 79.4, 74.2),
            TrendPoint("W8", 81.6, 75.5)
        ),
        records = listOf(
            rec("STU-01", "Aanya S.", "STEM Track", "studyHours" to 7.5, "attendance" to 95.0, "sleepHours" to 7.6, "testScore" to 88.0),
            rec("STU-02", "Rohan M.", "STEM Track", "studyHours" to 6.0, "attendance" to 91.0, "sleepHours" to 7.2, "testScore" to 80.0),
            rec("STU-03", "Diya K.", "Arts Track", "studyHours" to 8.2, "attendance" to 97.0, "sleepHours" to 7.8, "testScore" to 92.0),
            rec("STU-04", "Kabir V.", "Commerce", "studyHours" to 4.0, "attendance" to 82.0, "sleepHours" to 6.8, "testScore" to 67.0),
            rec("STU-05", "Meera P.", "STEM Track", "studyHours" to 7.0, "attendance" to 93.0, "sleepHours" to 7.4, "testScore" to 85.0),
            rec("STU-06", "Arjun T.", "Commerce", "studyHours" to 3.5, "attendance" to 78.0, "sleepHours" to 6.5, "testScore" to 61.0),
            rec("STU-07", "Sana R.", "Arts Track", "studyHours" to 5.5, "attendance" to 89.0, "sleepHours" to 7.5, "testScore" to 76.0),
            rec("STU-08", "Vikram N.", "STEM Track", "studyHours" to 8.0, "attendance" to 96.0, "sleepHours" to 7.9, "testScore" to 91.0),
            rec("STU-09", "Ishita B.", "Commerce", "studyHours" to 4.8, "attendance" to 85.0, "sleepHours" to 7.0, "testScore" to 71.0),
            rec("STU-10", "Devansh L.", "Arts Track", "studyHours" to 6.4, "attendance" to 90.0, "sleepHours" to 7.3, "testScore" to 79.0),
            rec("STU-11", "Neha G.", "STEM Track", "studyHours" to 7.8, "attendance" to 94.0, "sleepHours" to 7.7, "testScore" to 89.0),
            rec("STU-12", "Siddharth C.", "Commerce", "studyHours" to 5.2, "attendance" to 87.0, "sleepHours" to 7.1, "testScore" to 74.0),
            rec("STU-13", "Tara D.", "Arts Track", "studyHours" to 6.7, "attendance" to 92.0, "sleepHours" to 7.5, "testScore" to 83.0),
            rec("STU-14", "Aarav J. (Burnout)", "STEM Track", "studyHours" to 12.6, "attendance" to 96.0, "sleepHours" to 4.1, "testScore" to 64.0),
            rec("STU-15", "Priya H.", "Commerce", "studyHours" to 4.4, "attendance" to 84.0, "sleepHours" to 6.9, "testScore" to 68.0),
            rec("STU-16", "Karan W.", "Arts Track", "studyHours" to 5.8, "attendance" to 88.0, "sleepHours" to 7.2, "testScore" to 77.0),
            rec("STU-17", "Ananya Z.", "STEM Track", "studyHours" to 8.6, "attendance" to 98.0, "sleepHours" to 8.1, "testScore" to 94.0),
            rec("STU-18", "Nikhil Q.", "Commerce", "studyHours" to 3.2, "attendance" to 54.0, "sleepHours" to 6.2, "testScore" to 49.0),
            rec("STU-19", "Riya E.", "Arts Track", "studyHours" to 6.2, "attendance" to 91.0, "sleepHours" to 7.4, "testScore" to 81.0),
            rec("STU-20", "Aditya F.", "STEM Track", "studyHours" to 7.2, "attendance" to 93.0, "sleepHours" to 7.6, "testScore" to 86.0),
            rec("STU-21", "Zoya U.", "Commerce", "studyHours" to 5.0, "attendance" to 86.0, "sleepHours" to 7.0, "testScore" to 73.0),
            rec("STU-22", "Pranav I.", "Arts Track", "studyHours" to 4.6, "attendance" to 83.0, "sleepHours" to 6.7, "testScore" to 69.0),
            rec("STU-23", "Kavya O.", "STEM Track", "studyHours" to 8.4, "attendance" to 97.0, "sleepHours" to 8.0, "testScore" to 93.0),
            rec("STU-24", "Harsh Y.", "Commerce", "studyHours" to 2.8, "attendance" to 58.0, "sleepHours" to 6.4, "testScore" to 52.0)
        )
    )

    val footballAnalytics = DatasetSpec(
        id = "football",
        name = "Football Analytics",
        shortName = "Football",
        category = "Sports Performance",
        description = "Synthetic league scouting dataset evaluating 22 attacking and midfield players across goals, assists, passing accuracy, shot volume, and minutes played.",
        primaryTarget = "goals",
        defaultX = "shots",
        defaultY = "goals",
        metrics = listOf(
            MetricSpec("goals", "Goals Scored", "", CyanGlow, 0.0, 30.0, 1.0),
            MetricSpec("assists", "Assists", "", VioletGlow, 0.0, 20.0, 1.0),
            MetricSpec("passingAccuracy", "Passing Accuracy", "%", ElectricBlue, 55.0, 96.0, 1.0),
            MetricSpec("shots", "Total Shots", "", AmberGlow, 10.0, 130.0, 2.0),
            MetricSpec("minutesPlayed", "Minutes Played", "m", EmeraldGlow, 600.0, 3200.0, 50.0)
        ),
        predictors = listOf("shots", "passingAccuracy", "minutesPlayed"),
        suggestedQuestions = listOf(
            "Which variable has the strongest relationship with goals scored?",
            "What patterns can you identify across positions?",
            "Are there any unusual player observations?",
            "What conclusions can we draw for scouting?",
            "What would happen if a player took more shots?"
        ),
        trendTitle = "Squad Cumulative Expected vs Actual Goals (MD 5–35)",
        trendData = listOf(
            TrendPoint("MD 5", 18.0, 16.0),
            TrendPoint("MD 10", 37.0, 34.0),
            TrendPoint("MD 15", 59.0, 53.0),
            TrendPoint("MD 20", 78.0, 72.0),
            TrendPoint("MD 25", 104.0, 92.0),
            TrendPoint("MD 30", 129.0, 114.0),
            TrendPoint("MD 35", 158.0, 138.0)
        ),
        records = listOf(
            rec("PLY-01", "L. Alvarez", "Forward", "goals" to 14.0, "assists" to 6.0, "passingAccuracy" to 79.0, "shots" to 72.0, "minutesPlayed" to 2650.0),
            rec("PLY-02", "M. Silva", "Midfielder", "goals" to 7.0, "assists" to 11.0, "passingAccuracy" to 88.0, "shots" to 44.0, "minutesPlayed" to 2810.0),
            rec("PLY-03", "K. Mensah", "Forward", "goals" to 12.0, "assists" to 4.0, "passingAccuracy" to 76.0, "shots" to 65.0, "minutesPlayed" to 2430.0),
            rec("PLY-04", "T. Becker", "Midfielder", "goals" to 5.0, "assists" to 9.0, "passingAccuracy" to 86.0, "shots" to 34.0, "minutesPlayed" to 2590.0),
            rec("PLY-05", "R. Takahashi", "Winger", "goals" to 10.0, "assists" to 8.0, "passingAccuracy" to 82.0, "shots" to 56.0, "minutesPlayed" to 2490.0),
            rec("PLY-06", "J. Okafor", "Forward", "goals" to 11.0, "assists" to 3.0, "passingAccuracy" to 75.0, "shots" to 61.0, "minutesPlayed" to 2310.0),
            rec("PLY-07", "E. Moreau", "Midfielder", "goals" to 4.0, "assists" to 10.0, "passingAccuracy" to 89.0, "shots" to 28.0, "minutesPlayed" to 2740.0),
            rec("PLY-08", "D. Kowalski", "Winger", "goals" to 9.0, "assists" to 7.0, "passingAccuracy" to 81.0, "shots" to 51.0, "minutesPlayed" to 2380.0),
            rec("PLY-09", "A. Diallo", "Forward", "goals" to 27.0, "assists" to 5.0, "passingAccuracy" to 78.0, "shots" to 118.0, "minutesPlayed" to 2980.0),
            rec("PLY-10", "S. Verma", "Midfielder", "goals" to 6.0, "assists" to 8.0, "passingAccuracy" to 85.0, "shots" to 39.0, "minutesPlayed" to 2520.0),
            rec("PLY-11", "C. Rossi", "Winger", "goals" to 8.0, "assists" to 6.0, "passingAccuracy" to 80.0, "shots" to 48.0, "minutesPlayed" to 2210.0),
            rec("PLY-12", "H. Lindqvist", "Midfielder", "goals" to 3.0, "assists" to 7.0, "passingAccuracy" to 87.0, "shots" to 24.0, "minutesPlayed" to 2450.0),
            rec("PLY-13", "F. Santos", "Forward", "goals" to 13.0, "assists" to 5.0, "passingAccuracy" to 77.0, "shots" to 68.0, "minutesPlayed" to 2540.0),
            rec("PLY-14", "B. Gallagher", "Winger", "goals" to 7.0, "assists" to 4.0, "passingAccuracy" to 60.0, "shots" to 49.0, "minutesPlayed" to 1960.0),
            rec("PLY-15", "N. Petrov", "Midfielder", "goals" to 5.0, "assists" to 12.0, "passingAccuracy" to 90.0, "shots" to 31.0, "minutesPlayed" to 2890.0),
            rec("PLY-16", "Y. Benali", "Forward", "goals" to 10.0, "assists" to 4.0, "passingAccuracy" to 76.0, "shots" to 57.0, "minutesPlayed" to 2150.0),
            rec("PLY-17", "G. Fernandez", "Midfielder", "goals" to 6.0, "assists" to 9.0, "passingAccuracy" to 84.0, "shots" to 41.0, "minutesPlayed" to 2630.0),
            rec("PLY-18", "O. Wright", "Winger", "goals" to 9.0, "assists" to 5.0, "passingAccuracy" to 79.0, "shots" to 53.0, "minutesPlayed" to 2290.0),
            rec("PLY-19", "P. Novak", "Forward", "goals" to 8.0, "assists" to 2.0, "passingAccuracy" to 74.0, "shots" to 46.0, "minutesPlayed" to 1840.0),
            rec("PLY-20", "I. Costa", "Midfielder", "goals" to 4.0, "assists" to 8.0, "passingAccuracy" to 86.0, "shots" to 29.0, "minutesPlayed" to 2410.0),
            rec("PLY-21", "V. Sterling", "Winger", "goals" to 11.0, "assists" to 7.0, "passingAccuracy" to 81.0, "shots" to 59.0, "minutesPlayed" to 2560.0),
            rec("PLY-22", "Z. Demir", "Forward", "goals" to 6.0, "assists" to 3.0, "passingAccuracy" to 77.0, "shots" to 37.0, "minutesPlayed" to 1120.0)
        )
    )

    val healthcareResearch = DatasetSpec(
        id = "healthcare",
        name = "Healthcare Research",
        shortName = "Healthcare",
        category = "Clinical Trials (Synthetic)",
        description = "Synthetic clinical recovery study across 24 patients comparing Standard Care, Targeted Therapy, and Rehab Plus protocols by age, baseline severity, recovery duration, and outcome score.",
        primaryTarget = "outcomeScore",
        defaultX = "recoveryDays",
        defaultY = "outcomeScore",
        metrics = listOf(
            MetricSpec("age", "Patient Age", "yrs", ElectricBlue, 20.0, 85.0, 1.0),
            MetricSpec("severityIndex", "Baseline Severity", "pts", AmberGlow, 1.0, 10.0, 0.5),
            MetricSpec("recoveryDays", "Recovery Duration", "d", VioletGlow, 5.0, 45.0, 1.0),
            MetricSpec("outcomeScore", "Outcome Health Score", "/100", CyanGlow, 30.0, 100.0, 1.0)
        ),
        predictors = listOf("age", "severityIndex", "recoveryDays"),
        suggestedQuestions = listOf(
            "Which variable has the strongest relationship with outcome score?",
            "How do the different treatment protocols compare?",
            "Are there any unusual patient observations?",
            "What patterns exist across age groups?",
            "What conclusions can we draw from this synthetic trial?"
        ),
        trendTitle = "Mean Patient Mobility & Recovery Index (Weeks 1–6)",
        trendData = listOf(
            TrendPoint("W1", 44.0, 41.0),
            TrendPoint("W2", 56.0, 50.0),
            TrendPoint("W3", 68.0, 59.0),
            TrendPoint("W4", 77.0, 66.0),
            TrendPoint("W5", 84.0, 72.0),
            TrendPoint("W6", 89.0, 76.0)
        ),
        records = listOf(
            rec("PAT-01", "Case #101", "Targeted Therapy", "age" to 34.0, "severityIndex" to 4.5, "recoveryDays" to 11.0, "outcomeScore" to 91.0),
            rec("PAT-02", "Case #102", "Standard Care", "age" to 52.0, "severityIndex" to 6.0, "recoveryDays" to 19.0, "outcomeScore" to 74.0),
            rec("PAT-03", "Case #103", "Rehab Plus", "age" to 41.0, "severityIndex" to 5.0, "recoveryDays" to 13.0, "outcomeScore" to 86.0),
            rec("PAT-04", "Case #104", "Targeted Therapy", "age" to 29.0, "severityIndex" to 3.5, "recoveryDays" to 9.0, "outcomeScore" to 94.0),
            rec("PAT-05", "Case #105", "Standard Care", "age" to 64.0, "severityIndex" to 7.0, "recoveryDays" to 23.0, "outcomeScore" to 68.0),
            rec("PAT-06", "Case #106", "Rehab Plus", "age" to 47.0, "severityIndex" to 5.5, "recoveryDays" to 14.0, "outcomeScore" to 84.0),
            rec("PAT-07", "Case #107", "Targeted Therapy", "age" to 58.0, "severityIndex" to 6.5, "recoveryDays" to 15.0, "outcomeScore" to 83.0),
            rec("PAT-08", "Case #108", "Standard Care", "age" to 38.0, "severityIndex" to 5.0, "recoveryDays" to 17.0, "outcomeScore" to 77.0),
            rec("PAT-09", "Case #109", "Rehab Plus", "age" to 62.0, "severityIndex" to 6.5, "recoveryDays" to 17.0, "outcomeScore" to 79.0),
            rec("PAT-10", "Case #110", "Targeted Therapy", "age" to 45.0, "severityIndex" to 4.8, "recoveryDays" to 12.0, "outcomeScore" to 89.0),
            rec("PAT-11", "Case #111", "Standard Care", "age" to 71.0, "severityIndex" to 8.5, "recoveryDays" to 37.0, "outcomeScore" to 52.0),
            rec("PAT-12", "Case #112", "Rehab Plus", "age" to 33.0, "severityIndex" to 4.0, "recoveryDays" to 10.0, "outcomeScore" to 90.0),
            rec("PAT-13", "Case #113", "Targeted Therapy", "age" to 50.0, "severityIndex" to 5.2, "recoveryDays" to 13.0, "outcomeScore" to 87.0),
            rec("PAT-14", "Case #114", "Standard Care", "age" to 55.0, "severityIndex" to 6.2, "recoveryDays" to 20.0, "outcomeScore" to 72.0),
            rec("PAT-15", "Case #115", "Rehab Plus", "age" to 66.0, "severityIndex" to 7.2, "recoveryDays" to 18.0, "outcomeScore" to 78.0),
            rec("PAT-16", "Case #116", "Targeted Therapy", "age" to 39.0, "severityIndex" to 4.2, "recoveryDays" to 10.0, "outcomeScore" to 92.0),
            rec("PAT-17", "Case #117", "Standard Care", "age" to 69.0, "severityIndex" to 8.8, "recoveryDays" to 39.0, "outcomeScore" to 49.0),
            rec("PAT-18", "Case #118", "Rehab Plus", "age" to 43.0, "severityIndex" to 5.4, "recoveryDays" to 14.0, "outcomeScore" to 85.0),
            rec("PAT-19", "Case #119", "Targeted Therapy", "age" to 61.0, "severityIndex" to 6.8, "recoveryDays" to 16.0, "outcomeScore" to 81.0),
            rec("PAT-20", "Case #120", "Standard Care", "age" to 46.0, "severityIndex" to 5.5, "recoveryDays" to 18.0, "outcomeScore" to 75.0),
            rec("PAT-21", "Case #121", "Rehab Plus", "age" to 36.0, "severityIndex" to 4.5, "recoveryDays" to 11.0, "outcomeScore" to 88.0),
            rec("PAT-22", "Case #122", "Targeted Therapy", "age" to 54.0, "severityIndex" to 5.8, "recoveryDays" to 14.0, "outcomeScore" to 85.0),
            rec("PAT-23", "Case #123", "Standard Care", "age" to 49.0, "severityIndex" to 6.0, "recoveryDays" to 19.0, "outcomeScore" to 73.0),
            rec("PAT-24", "Case #124", "Rehab Plus", "age" to 57.0, "severityIndex" to 6.0, "recoveryDays" to 15.0, "outcomeScore" to 82.0)
        )
    )

    val all: List<DatasetSpec> = listOf(studentPerformance, footballAnalytics, healthcareResearch)

    fun byId(id: String): DatasetSpec = all.firstOrNull { it.id == id } ?: studentPerformance

    private fun rec(
        id: String,
        name: String,
        group: String,
        vararg pairs: Pair<String, Double>
    ): DataRecord = DataRecord(id = id, name = name, group = group, values = pairs.toMap())
}
