package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.analytics.AnalyticsEngine
import com.example.data.DatasetSpec
import com.example.data.DatasetsRepository
import com.example.ui.components.AreaTrendChart
import com.example.ui.components.BadgePill
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowPrimaryButton
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletGlow
import com.example.viewmodel.AppModule

@Composable
private fun AnimatedMetricValue(
    target: Float,
    decimals: Int = 0,
    color: Color = Slate100
) {
    var started by remember(target) { mutableStateOf(false) }
    LaunchedEffect(target) { started = true }
    val animated by animateFloatAsState(
        targetValue = if (started) target else 0f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "kpi_counter"
    )
    Text(
        text = if (decimals == 0) animated.toInt().toString() else String.format("%.${decimals}f", animated),
        style = MaterialTheme.typography.headlineMedium,
        color = color
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardHomeScreen(
    activeDataset: DatasetSpec,
    onSelectDataset: (String) -> Unit,
    onNavigate: (AppModule) -> Unit,
    modifier: Modifier = Modifier
) {
    val allDatasets = DatasetsRepository.all
    val totalRecords = remember { allDatasets.sumOf { it.records.size } }
    val activeAnomalies = remember(activeDataset.id) { AnalyticsEngine.scanAllAnomalies(activeDataset) }
    val topCorr = remember(activeDataset.id) { AnalyticsEngine.computeCorrelationPairs(activeDataset).firstOrNull() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Banner with generated neural background art
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Navy900)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_neural_1795626283265),
                    contentDescription = "Neural data visualization backdrop",
                    contentScale = ContentScale.Crop,
                    alpha = 0.34f,
                    modifier = Modifier.matchParentSize()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Navy950.copy(alpha = 0.65f),
                                    Navy900.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BadgePill(
                        text = stringResource(R.string.hero_badge),
                        accentColor = CyanGlow
                    )

                    Text(
                        text = "Welcome to the World of Intelligent Data.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyanGlow.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DATAVISION ",
                            style = MaterialTheme.typography.displaySmall,
                            color = Slate100
                        )
                        Text(
                            text = "AI",
                            style = MaterialTheme.typography.displaySmall,
                            color = CyanGlow
                        )
                    }

                    Text(
                        text = stringResource(R.string.tagline),
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate300
                    )

                    Text(
                        text = "Explore live synthetic datasets in Education, Sports, and Healthcare. Uncover hidden correlations, isolate statistical outliers, test OLS regression, and query our Gemini AI Analyst.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        GlowPrimaryButton(
                            text = stringResource(R.string.cta_start_analysing),
                            onClick = { onNavigate(AppModule.LAB) },
                            modifier = Modifier.testTag("start_analysing_button"),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Navy950,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        OutlinedButton(
                            onClick = { onNavigate(AppModule.CAREER) },
                            border = BorderStroke(1.dp, VioletGlow.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier
                                .testTag("launch_career_fair_button")
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = VioletGlow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.cta_launch_career_fair),
                                style = MaterialTheme.typography.labelLarge,
                                color = Slate100
                            )
                        }
                    }
                }
            }
        }

        // Live Active Stream Card
        item {
            GlassCard(borderColor = CyanGlow.copy(alpha = 0.25f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "LIVE ACTIVE STREAM",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanGlow
                        )
                        Text(
                            text = activeDataset.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = Slate100
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = EmeraldGlow.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, EmeraldGlow.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGlow)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Gemini AI Ready",
                                style = MaterialTheme.typography.labelSmall,
                                color = EmeraldGlow
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                AreaTrendChart(
                    data = activeDataset.trendData,
                    height = 160.dp
                )
                Spacer(Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allDatasets.forEach { ds ->
                        val selected = ds.id == activeDataset.id
                        Surface(
                            modifier = Modifier
                                .testTag("hero_dataset_${ds.id}")
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onSelectDataset(ds.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) CyanGlow.copy(alpha = 0.2f) else Navy950.copy(alpha = 0.7f),
                            border = BorderStroke(
                                1.dp,
                                if (selected) CyanGlow.copy(alpha = 0.6f) else Slate400.copy(alpha = 0.2f)
                            )
                        ) {
                            Text(
                                text = ds.shortName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (selected) CyanGlow else Slate300,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Animated KPI Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiStatCard(
                        title = "TOTAL RECORDS",
                        subtitle = "Across 3 domain datasets",
                        icon = Icons.Filled.Storage,
                        accent = CyanGlow,
                        modifier = Modifier.weight(1f)
                    ) {
                        AnimatedMetricValue(totalRecords.toFloat(), decimals = 0, color = Slate100)
                    }

                    KpiStatCard(
                        title = "TOP CORRELATION",
                        subtitle = if (topCorr != null) "${topCorr.labelA} ↔ ${topCorr.labelB}" else "",
                        icon = Icons.Filled.Analytics,
                        accent = ElectricBlue,
                        modifier = Modifier.weight(1f)
                    ) {
                        AnimatedMetricValue((topCorr?.absR ?: 0.0).toFloat(), decimals = 2, color = CyanGlow)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KpiStatCard(
                        title = "IQR ANOMALIES",
                        subtitle = "In ${activeDataset.shortName} dataset",
                        icon = Icons.Filled.Shield,
                        accent = AmberGlow,
                        modifier = Modifier.weight(1f)
                    ) {
                        AnimatedMetricValue(activeAnomalies.size.toFloat(), decimals = 0, color = AmberGlow)
                    }

                    KpiStatCard(
                        title = "AI & ML MODULES",
                        subtitle = "Charts, Gemini, IQR, OLS",
                        icon = Icons.Filled.AutoAwesome,
                        accent = VioletGlow,
                        modifier = Modifier.weight(1f)
                    ) {
                        AnimatedMetricValue(6f, decimals = 0, color = VioletGlow)
                    }
                }
            }
        }

        // Platform Modules Launchpad
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Platform Modules",
                    style = MaterialTheme.typography.titleLarge,
                    color = Slate100
                )
                Text(
                    text = "Insight Generator →",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyanGlow,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigate(AppModule.INSIGHTS) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ModuleLaunchCard(
                    title = "Live Analytics Lab",
                    description = "Inspect descriptive stats, Bar, Line & Scatter charts, and Pearson correlations.",
                    icon = Icons.Filled.Analytics,
                    accent = CyanGlow,
                    testTag = "module_card_lab",
                    onClick = { onNavigate(AppModule.LAB) }
                )
                ModuleLaunchCard(
                    title = "AI Data Analyst",
                    description = "Ask natural-language questions powered by Google Gemini & verified dataset stats.",
                    icon = Icons.Filled.Psychology,
                    accent = VioletGlow,
                    testTag = "module_card_ai",
                    onClick = { onNavigate(AppModule.AI) }
                )
                ModuleLaunchCard(
                    title = "Anomaly Engine",
                    description = "Detect statistical outliers transparently with 1.5 × IQR threshold fences.",
                    icon = Icons.Filled.Shield,
                    accent = AmberGlow,
                    testTag = "module_card_anomaly",
                    onClick = { onNavigate(AppModule.ANOMALY) }
                )
                ModuleLaunchCard(
                    title = "Prediction Lab",
                    description = "Tune predictor sliders live to simulate outcomes via OLS Multiple Regression.",
                    icon = Icons.Filled.TrendingUp,
                    accent = EmeraldGlow,
                    testTag = "module_card_predict",
                    onClick = { onNavigate(AppModule.PREDICT) }
                )
                ModuleLaunchCard(
                    title = "Insight Generator",
                    description = "Auto-extract 3 hidden discoveries with scatter and cohort comparison charts.",
                    icon = Icons.Filled.Bolt,
                    accent = ElectricBlue,
                    testTag = "module_card_insights",
                    onClick = { onNavigate(AppModule.INSIGHTS) }
                )
            }
        }
    }
}

@Composable
private fun KpiStatCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    valueContent: @Composable () -> Unit
) {
    GlassCard(
        modifier = modifier,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Slate400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        valueContent()
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Slate400,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ModuleLaunchCard(
    title: String,
    description: String,
    icon: ImageVector,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        borderColor = accent.copy(alpha = 0.25f),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = accent.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.38f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate100
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Open $title",
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
