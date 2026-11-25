package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.analytics.AnalyticsEngine
import com.example.data.DatasetSpec
import com.example.data.DatasetsRepository
import com.example.ui.components.BadgePill
import com.example.ui.components.BarDatum
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowPrimaryButton
import com.example.ui.components.MetricBarChart
import com.example.ui.components.RichBoldText
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.Navy950
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletGlow
import com.example.viewmodel.AiCallState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CareerFairModeScreen(
    activeDataset: DatasetSpec,
    step: Int,
    activeQuestion: String,
    careerAiState: AiCallState,
    onSetStep: (Int) -> Unit,
    onSelectDataset: (String) -> Unit,
    onCycleDataset: () -> Unit,
    onAskDemo: (String) -> Unit,
    onRunLocalFallback: (String) -> Unit,
    onExitCareerMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allDatasets = DatasetsRepository.all
    val stats = remember(activeDataset.id) { AnalyticsEngine.computeMetricStats(activeDataset) }
    val targetMetric = remember(activeDataset.id) { activeDataset.metricByKey(activeDataset.primaryTarget) }
    val stepLabels = listOf("Welcome", "1. Explore", "2. Ask AI", "3. Impact")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stepper & Cycle Header
        item {
            GlassCard(borderColor = VioletGlow.copy(alpha = 0.35f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = VioletGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "CAREER FAIR MODE",
                            style = MaterialTheme.typography.labelMedium,
                            color = VioletGlow
                        )
                    }
                    OutlinedButton(
                        onClick = onCycleDataset,
                        border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.5f)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .testTag("explore_another_dataset_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.cta_explore_another),
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanGlow
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stepLabels.forEachIndexed { idx, label ->
                        val active = step == idx
                        Surface(
                            modifier = Modifier
                                .testTag("career_step_$idx")
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSetStep(idx) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) CyanGlow.copy(alpha = 0.2f) else Navy950.copy(alpha = 0.7f),
                            border = BorderStroke(
                                1.dp,
                                if (active) CyanGlow.copy(alpha = 0.55f) else Slate400.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (active) CyanGlow else Slate400,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        when (step) {
            // Stage 0: Welcome
            0 -> {
                item {
                    GlassCard(borderColor = CyanGlow.copy(alpha = 0.28f)) {
                        BadgePill(text = "SCHOOL CAREER FAIR • LIVE DEMO", accentColor = VioletGlow)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Discover What Data Hides.",
                            style = MaterialTheme.typography.displaySmall,
                            color = Slate100
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Pick a real-world domain below, inspect live charts, ask Gemini AI a question, and see how Data Science transforms everyday decisions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate300
                        )
                        Spacer(Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            allDatasets.forEach { ds ->
                                GlassCard(
                                    borderColor = if (ds.id == activeDataset.id) CyanGlow.copy(alpha = 0.5f) else Slate400.copy(alpha = 0.2f),
                                    contentPadding = PaddingValues(14.dp),
                                    onClick = {
                                        onSelectDataset(ds.id)
                                        onSetStep(1)
                                    }
                                ) {
                                    Text(
                                        text = ds.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyanGlow
                                    )
                                    Text(
                                        text = ds.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Slate100
                                    )
                                    Text(
                                        text = "${ds.records.size} synthetic records ready",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate400
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        GlowPrimaryButton(
                            text = "Begin Guided Tour",
                            onClick = { onSetStep(1) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Navy950,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Stage 1: Explore Data
            1 -> {
                item {
                    GlassCard {
                        Text(
                            text = "STEP 1 • DATASET EXPLORATION",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanGlow
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Exploring: ${activeDataset.name}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Slate100
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = activeDataset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate400
                        )
                        Spacer(Modifier.height(14.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            stats.forEach { s ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Navy950.copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, Slate400.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(s.label, style = MaterialTheme.typography.labelSmall, color = Slate400)
                                        Text(
                                            text = "${s.mean}${s.unit}",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = CyanGlow
                                        )
                                        Text(
                                            text = "Median ${s.median}${s.unit} • ${s.min}–${s.max}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        MetricBarChart(
                            bars = activeDataset.records.take(12).map {
                                BarDatum(it.name.substringBefore(" "), it.name, it.getValue(targetMetric.key))
                            },
                            unit = targetMetric.unit,
                            primaryColor = CyanGlow,
                            height = 175.dp
                        )
                        Spacer(Modifier.height(14.dp))
                        GlowPrimaryButton(
                            text = "Next: Ask AI Analyst",
                            onClick = { onSetStep(2) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Navy950,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Stage 2: Ask AI Analyst
            2 -> {
                item {
                    GlassCard {
                        Text(
                            text = "STEP 2 • LIVE AI ANALYSIS DEMO",
                            style = MaterialTheme.typography.labelMedium,
                            color = VioletGlow
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Tap a Question for ${activeDataset.shortName}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Slate100
                        )
                        Spacer(Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeDataset.suggestedQuestions.forEach { q ->
                                val selected = activeQuestion == q
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onAskDemo(q) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) CyanGlow.copy(alpha = 0.2f) else Navy950.copy(alpha = 0.8f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) CyanGlow else Slate400.copy(alpha = 0.25f)
                                    )
                                ) {
                                    Text(
                                        text = q,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (selected) Slate100 else Slate300,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Navy950.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Slate400.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                when (careerAiState) {
                                    is AiCallState.Idle -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.Explore,
                                                contentDescription = null,
                                                tint = VioletGlow,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "Tap any question above to run live Gemini AI analysis on ${activeDataset.name}.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Slate400
                                            )
                                        }
                                    }

                                    is AiCallState.Loading -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = CyanGlow
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = "Computing evidence and querying Gemini...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = CyanGlow
                                            )
                                        }
                                    }

                                    is AiCallState.Streaming -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.SmartToy, null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("DATAVISION AI (Streaming):", style = MaterialTheme.typography.labelMedium, color = CyanGlow)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        RichBoldText(text = careerAiState.text)
                                    }

                                    is AiCallState.Success -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.SmartToy, null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("DATAVISION AI Response:", style = MaterialTheme.typography.labelMedium, color = CyanGlow)
                                        }
                                        Spacer(Modifier.height(6.dp))
                                        RichBoldText(text = careerAiState.text)
                                    }

                                    is AiCallState.Error -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.ErrorOutline, null, tint = RoseError, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Gemini Error: ${careerAiState.message}", style = MaterialTheme.typography.bodySmall, color = RoseError)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = { onRunLocalFallback(careerAiState.lastQuestion) },
                                            border = BorderStroke(1.dp, VioletGlow.copy(alpha = 0.5f))
                                        ) {
                                            Icon(Icons.Filled.Memory, null, tint = VioletGlow, modifier = Modifier.size(15.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Run Local Statistical Engine", color = Slate100)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        GlowPrimaryButton(
                            text = "Next: Real-World Impact",
                            onClick = { onSetStep(3) },
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Navy950,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }

            // Stage 3: Impact
            else -> {
                item {
                    GlassCard(borderColor = EmeraldGlow.copy(alpha = 0.35f)) {
                        Text(
                            text = "STEP 3 • CAREER FAIR TAKEAWAY",
                            style = MaterialTheme.typography.labelMedium,
                            color = EmeraldGlow
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "How Data Science Transforms Decisions",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Slate100
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Behind every smart recommendation, medical trial, and championship roster is a Data Scientist asking better questions and checking evidence.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate300
                        )
                        Spacer(Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            ImpactCard(
                                title = "In Education",
                                body = "Schools spot attendance dips and study burnout patterns early so mentors can step in before grades drop.",
                                icon = Icons.Filled.School,
                                color = CyanGlow
                            )
                            ImpactCard(
                                title = "In Sports",
                                body = "Clubs analyze shot volume, passing accuracy, and workload minutes to scout talent and prevent injury.",
                                icon = Icons.Filled.EmojiEvents,
                                color = VioletGlow
                            )
                            ImpactCard(
                                title = "In Healthcare",
                                body = "Researchers compare treatment recovery curves and flag rare outlier reactions early for safer care.",
                                icon = Icons.Filled.Favorite,
                                color = EmeraldGlow
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        GlowPrimaryButton(
                            text = stringResource(R.string.cta_explore_another),
                            onClick = onCycleDataset,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = null,
                                    tint = Navy950,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onExitCareerMode,
                            border = BorderStroke(1.dp, Slate400.copy(alpha = 0.35f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Return to Full Platform",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate200
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpactCard(
    title: String,
    body: String,
    icon: ImageVector,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Navy950.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = Slate100)
                Spacer(Modifier.height(2.dp))
                Text(text = body, style = MaterialTheme.typography.bodySmall, color = Slate400)
            }
        }
    }
}
