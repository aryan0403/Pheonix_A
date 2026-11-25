package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DatasetsRepository
import com.example.firebase.configureAppCheck
import com.example.ui.screens.AiAnalystScreen
import com.example.ui.screens.AnalyticsLabScreen
import com.example.ui.screens.AnomalyEngineScreen
import com.example.ui.screens.CareerFairModeScreen
import com.example.ui.screens.DashboardHomeScreen
import com.example.ui.screens.InsightGeneratorScreen
import com.example.ui.screens.PredictionLabScreen
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.DatavisionTheme
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletGlow
import com.example.viewmodel.AppModule
import com.example.viewmodel.DatavisionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Initialize Firebase App Check with launch intent before any UI or ViewModels.
        configureAppCheck(this, intent)

        // STEP 2: Enable full-bleed edge-to-edge UI and render Compose hierarchy.
        enableEdgeToEdge()
        setContent {
            DatavisionTheme {
                DatavisionApp()
            }
        }
    }
}

private data class NavEntry(
    val module: AppModule,
    val shortLabel: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
)

private val PRIMARY_TABS = listOf(
    NavEntry(AppModule.HOME, "Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    NavEntry(AppModule.LAB, "Lab", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    NavEntry(AppModule.AI, "AI", Icons.Filled.Psychology, Icons.Outlined.Psychology),
    NavEntry(AppModule.ANOMALY, "Outliers", Icons.Filled.Shield, Icons.Outlined.Shield),
    NavEntry(AppModule.PREDICT, "Predict", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp)
)

private val ALL_RAIL_TABS = PRIMARY_TABS + listOf(
    NavEntry(AppModule.INSIGHTS, "Insights", Icons.Filled.Bolt, Icons.Outlined.Bolt),
    NavEntry(AppModule.CAREER, "Fair", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
)

@Composable
fun DatavisionApp(
    viewModel: DatavisionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeDataset = uiState.activeDataset

    BackHandler(enabled = uiState.activeModule != AppModule.HOME) {
        if (uiState.activeModule == AppModule.CAREER && uiState.careerStep > 0) {
            viewModel.setCareerStep(uiState.careerStep - 1)
        } else {
            viewModel.navigateTo(AppModule.HOME)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Navy950, Navy900.copy(alpha = 0.92f), Navy950)
                )
            )
    ) {
        val isWideScreen = maxWidth >= 680.dp

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                DatavisionTopBar(
                    activeDatasetId = uiState.activeDatasetId,
                    activeModule = uiState.activeModule,
                    showExtraModuleChips = !isWideScreen,
                    onSelectDataset = viewModel::selectDataset,
                    onNavigate = viewModel::navigateTo
                )
            },
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = Navy900.copy(alpha = 0.96f),
                        contentColor = Slate100,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        PRIMARY_TABS.forEach { entry ->
                            val selected = uiState.activeModule == entry.module
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(entry.module) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) entry.activeIcon else entry.inactiveIcon,
                                        contentDescription = entry.module.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = entry.shortLabel,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = CyanGlow,
                                    selectedTextColor = CyanGlow,
                                    indicatorColor = CyanGlow.copy(alpha = 0.16f),
                                    unselectedIconColor = Slate400,
                                    unselectedTextColor = Slate400
                                ),
                                modifier = Modifier.testTag("nav_${entry.module.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = Navy900.copy(alpha = 0.92f),
                        contentColor = Slate100,
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        Spacer(Modifier.size(8.dp))
                        ALL_RAIL_TABS.forEach { entry ->
                            val selected = uiState.activeModule == entry.module
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(entry.module) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) entry.activeIcon else entry.inactiveIcon,
                                        contentDescription = entry.module.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = entry.shortLabel,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = CyanGlow,
                                    selectedTextColor = CyanGlow,
                                    indicatorColor = CyanGlow.copy(alpha = 0.16f),
                                    unselectedIconColor = Slate400,
                                    unselectedTextColor = Slate400
                                )
                            )
                        }
                    }
                }

                AnimatedContent(
                    targetState = uiState.activeModule,
                    transitionSpec = {
                        fadeIn(tween(220)) togetherWith fadeOut(tween(180))
                    },
                    label = "module_switch",
                    modifier = Modifier.weight(1f)
                ) { module ->
                    when (module) {
                        AppModule.HOME -> DashboardHomeScreen(
                            activeDataset = activeDataset,
                            onSelectDataset = viewModel::selectDataset,
                            onNavigate = viewModel::navigateTo
                        )

                        AppModule.LAB -> AnalyticsLabScreen(dataset = activeDataset)

                        AppModule.AI -> AiAnalystScreen(
                            dataset = activeDataset,
                            messages = uiState.chatMessages,
                            aiState = uiState.aiState,
                            onAskQuestion = viewModel::askAiAnalyst,
                            onRunLocalFallback = viewModel::runLocalFallbackInChat
                        )

                        AppModule.ANOMALY -> AnomalyEngineScreen(dataset = activeDataset)

                        AppModule.PREDICT -> PredictionLabScreen(dataset = activeDataset)

                        AppModule.INSIGHTS -> InsightGeneratorScreen(
                            dataset = activeDataset,
                            insights = uiState.insightsByDataset[activeDataset.id].orEmpty(),
                            generatedTimestamp = uiState.insightTimestamps[activeDataset.id],
                            onGenerateInsights = {
                                val ts = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                                viewModel.generateInsightsForActiveDataset(ts)
                            }
                        )

                        AppModule.CAREER -> CareerFairModeScreen(
                            activeDataset = activeDataset,
                            step = uiState.careerStep,
                            activeQuestion = uiState.careerQuestion,
                            careerAiState = uiState.careerAiState,
                            onSetStep = viewModel::setCareerStep,
                            onSelectDataset = viewModel::selectDataset,
                            onCycleDataset = viewModel::cycleDatasetForCareerFair,
                            onAskDemo = viewModel::askCareerFairDemo,
                            onRunLocalFallback = viewModel::runCareerLocalFallback,
                            onExitCareerMode = { viewModel.navigateTo(AppModule.HOME) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DatavisionTopBar(
    activeDatasetId: String,
    activeModule: AppModule,
    showExtraModuleChips: Boolean,
    onSelectDataset: (String) -> Unit,
    onNavigate: (AppModule) -> Unit
) {
    Surface(
        color = Navy950.copy(alpha = 0.92f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onNavigate(AppModule.HOME) }
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(listOf(CyanGlow, ElectricBlue, VioletGlow))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "DATAVISION AI Logo",
                            tint = Navy950,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DATAVISION ",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Slate100
                            )
                            Text(
                                text = "AI",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CyanGlow
                            )
                        }
                        Text(
                            text = activeModule.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate400
                        )
                    }
                }

                if (showExtraModuleChips) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        HeaderModePill(
                            label = "Insights",
                            icon = Icons.Filled.Bolt,
                            selected = activeModule == AppModule.INSIGHTS,
                            accent = CyanGlow,
                            testTag = "top_insights_pill",
                            onClick = { onNavigate(AppModule.INSIGHTS) }
                        )
                        HeaderModePill(
                            label = "Fair",
                            icon = Icons.Filled.AutoAwesome,
                            selected = activeModule == AppModule.CAREER,
                            accent = VioletGlow,
                            testTag = "top_career_pill",
                            onClick = { onNavigate(AppModule.CAREER) }
                        )
                    }
                }
            }

            // Global Dataset Switcher Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Storage,
                    contentDescription = "Active Dataset",
                    tint = CyanGlow,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Dataset:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate400
                )
                DatasetsRepository.all.forEach { ds ->
                    val selected = ds.id == activeDatasetId
                    Surface(
                        modifier = Modifier
                            .testTag("global_dataset_${ds.id}")
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectDataset(ds.id) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selected) CyanGlow.copy(alpha = 0.2f) else Navy800.copy(alpha = 0.7f),
                        border = BorderStroke(
                            1.dp,
                            if (selected) CyanGlow.copy(alpha = 0.55f) else Slate400.copy(alpha = 0.2f)
                        )
                    ) {
                        Text(
                            text = ds.shortName,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) CyanGlow else Slate300,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = CyanGlow.copy(alpha = 0.16f))
        }
    }
}

@Composable
private fun HeaderModePill(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accent: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) accent.copy(alpha = 0.2f) else Navy800.copy(alpha = 0.75f),
        border = BorderStroke(1.dp, if (selected) accent else Slate400.copy(alpha = 0.22f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) accent else Slate300,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) accent else Slate300
            )
        }
    }
}
