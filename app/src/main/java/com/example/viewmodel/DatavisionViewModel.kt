package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.analytics.AnalyticsEngine
import com.example.analytics.GeneratedInsight
import com.example.data.DatasetSpec
import com.example.data.DatasetsRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "DatavisionViewModel"
private const val GEMINI_MODEL_NAME = "gemini-3.8-flash"

enum class AppModule(val label: String) {
    HOME("Dashboard"),
    LAB("Analytics Lab"),
    AI("AI Analyst"),
    ANOMALY("Anomalies"),
    PREDICT("Prediction Lab"),
    INSIGHTS("Insights"),
    CAREER("Career Fair")
}

sealed interface AiCallState {
    data object Idle : AiCallState
    data object Loading : AiCallState
    data class Streaming(val text: String) : AiCallState
    data class Success(val text: String) : AiCallState
    data class Error(val message: String, val lastQuestion: String) : AiCallState
}

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val sourceLabel: String = ""
)

data class DatavisionUiState(
    val activeDatasetId: String = DatasetsRepository.studentPerformance.id,
    val activeModule: AppModule = AppModule.HOME,
    val chatMessages: List<ChatMessage> = emptyList(),
    val aiState: AiCallState = AiCallState.Idle,
    val careerStep: Int = 0,
    val careerQuestion: String = "",
    val careerAiState: AiCallState = AiCallState.Idle,
    val insightsByDataset: Map<String, List<GeneratedInsight>> = emptyMap(),
    val insightTimestamps: Map<String, String> = emptyMap()
) {
    val activeDataset: DatasetSpec
        get() = DatasetsRepository.byId(activeDatasetId)
}

class DatavisionViewModel : ViewModel() {

    private val generativeModel by lazy {
        Firebase.ai.generativeModel(modelName = GEMINI_MODEL_NAME)
    }

    private val _uiState = MutableStateFlow(DatavisionUiState())
    val uiState: StateFlow<DatavisionUiState> = _uiState.asStateFlow()

    init {
        resetWelcomeMessage(DatasetsRepository.studentPerformance)
    }

    fun selectDataset(datasetId: String) {
        if (_uiState.value.activeDatasetId == datasetId) return
        val ds = DatasetsRepository.byId(datasetId)
        _uiState.update {
            it.copy(
                activeDatasetId = datasetId,
                aiState = AiCallState.Idle,
                careerQuestion = "",
                careerAiState = AiCallState.Idle
            )
        }
        resetWelcomeMessage(ds)
    }

    fun navigateTo(module: AppModule) {
        _uiState.update { it.copy(activeModule = module) }
    }

    fun setCareerStep(step: Int) {
        _uiState.update { it.copy(careerStep = step.coerceIn(0, 3)) }
    }

    fun cycleDatasetForCareerFair() {
        val all = DatasetsRepository.all
        val currentIdx = all.indexOfFirst { it.id == _uiState.value.activeDatasetId }
        val next = all[(currentIdx + 1) % all.size]
        selectDataset(next.id)
        _uiState.update { it.copy(careerStep = 1) }
    }

    fun generateInsightsForActiveDataset(timestamp: String) {
        val ds = _uiState.value.activeDataset
        val generated = AnalyticsEngine.generateHiddenInsights(ds)
        _uiState.update { state ->
            state.copy(
                insightsByDataset = state.insightsByDataset + (ds.id to generated),
                insightTimestamps = state.insightTimestamps + (ds.id to timestamp)
            )
        }
    }

    private fun resetWelcomeMessage(ds: DatasetSpec) {
        val welcome = ChatMessage(
            id = "welcome-${ds.id}",
            isUser = false,
            text = "Hello! I am the **DATAVISION AI Analyst** powered by Google Gemini. I am inspecting **${ds.name}** (**${ds.records.size} records**).\n\nTap a suggested question or ask your own—every insight is grounded in verified dataset numbers.",
            sourceLabel = "Gemini AI Ready ($GEMINI_MODEL_NAME)"
        )
        _uiState.update { it.copy(chatMessages = listOf(welcome)) }
    }

    fun askAiAnalyst(question: String) {
        val q = question.trim()
        if (q.isEmpty() || _uiState.value.aiState is AiCallState.Loading || _uiState.value.aiState is AiCallState.Streaming) {
            return
        }
        val dataset = _uiState.value.activeDataset
        val userMsg = ChatMessage(
            id = "u-${System.currentTimeMillis()}",
            isUser = true,
            text = q
        )
        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMsg,
                aiState = AiCallState.Loading
            )
        }

        val prompt = buildGroundedPrompt(q, dataset)
        val assistantMsgId = "a-${System.currentTimeMillis()}"

        viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    generativeModel.generateContentStream(prompt).collect { chunk ->
                        val part = chunk.text.orEmpty()
                        if (part.isNotEmpty()) {
                            accumulated.append(part)
                            val partialText = accumulated.toString()
                            _uiState.update { state ->
                                val withoutDraft = state.chatMessages.filterNot { it.id == assistantMsgId }
                                state.copy(
                                    aiState = AiCallState.Streaming(partialText),
                                    chatMessages = withoutDraft + ChatMessage(
                                        id = assistantMsgId,
                                        isUser = false,
                                        text = partialText,
                                        sourceLabel = "Streaming via Gemini ($GEMINI_MODEL_NAME)"
                                    )
                                )
                            }
                        }
                    }
                }
                val finalAnswer = accumulated.toString().trim()
                if (finalAnswer.isNotEmpty()) {
                    _uiState.update { state ->
                        val withoutDraft = state.chatMessages.filterNot { it.id == assistantMsgId }
                        state.copy(
                            aiState = AiCallState.Success(finalAnswer),
                            chatMessages = withoutDraft + ChatMessage(
                                id = assistantMsgId,
                                isUser = false,
                                text = finalAnswer,
                                sourceLabel = "Verified by Google Gemini ($GEMINI_MODEL_NAME)"
                            )
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(aiState = AiCallState.Error("Gemini returned an empty response.", q))
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Gemini AI request timed out", e)
                _uiState.update {
                    it.copy(
                        aiState = AiCallState.Error(
                            "Gemini AI request timed out after 60 seconds. Check connectivity or use the Local Statistical Engine.",
                            q
                        )
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Gemini AI generation failed", e)
                _uiState.update {
                    it.copy(
                        aiState = AiCallState.Error(
                            e.localizedMessage ?: "Unable to reach Gemini AI. Check network or App Check configuration.",
                            q
                        )
                    )
                }
            }
        }
    }

    fun runLocalFallbackInChat(question: String) {
        val dataset = _uiState.value.activeDataset
        val localText = AnalyticsEngine.runLocalAnalysis(question, dataset)
        val msg = ChatMessage(
            id = "local-${System.currentTimeMillis()}",
            isUser = false,
            text = localText,
            sourceLabel = "Local Statistical Engine (Offline)"
        )
        _uiState.update {
            it.copy(
                aiState = AiCallState.Success(localText),
                chatMessages = it.chatMessages + msg
            )
        }
    }

    fun askCareerFairDemo(question: String) {
        val q = question.trim()
        if (q.isEmpty() || _uiState.value.careerAiState is AiCallState.Loading) return
        val dataset = _uiState.value.activeDataset
        _uiState.update {
            it.copy(careerQuestion = q, careerAiState = AiCallState.Loading)
        }
        val prompt = buildGroundedPrompt(q, dataset)
        viewModelScope.launch {
            val accumulated = StringBuilder()
            try {
                withTimeout(60_000L) {
                    generativeModel.generateContentStream(prompt).collect { chunk ->
                        val part = chunk.text.orEmpty()
                        if (part.isNotEmpty()) {
                            accumulated.append(part)
                            _uiState.update {
                                it.copy(careerAiState = AiCallState.Streaming(accumulated.toString()))
                            }
                        }
                    }
                }
                val finalAnswer = accumulated.toString().trim()
                if (finalAnswer.isNotEmpty()) {
                    _uiState.update { it.copy(careerAiState = AiCallState.Success(finalAnswer)) }
                } else {
                    _uiState.update {
                        it.copy(careerAiState = AiCallState.Error("Gemini returned an empty response.", q))
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Career Fair AI timed out", e)
                _uiState.update {
                    it.copy(careerAiState = AiCallState.Error("Request timed out after 60s.", q))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Career Fair AI failed", e)
                _uiState.update {
                    it.copy(
                        careerAiState = AiCallState.Error(
                            e.localizedMessage ?: "Unable to reach Gemini AI.",
                            q
                        )
                    )
                }
            }
        }
    }

    fun runCareerLocalFallback(question: String) {
        val dataset = _uiState.value.activeDataset
        val localText = AnalyticsEngine.runLocalAnalysis(question, dataset)
        _uiState.update { it.copy(careerAiState = AiCallState.Success(localText)) }
    }

    private fun buildGroundedPrompt(question: String, dataset: DatasetSpec): String {
        val dossier = AnalyticsEngine.buildDatasetDossier(dataset)
        return """You are DATAVISION AI's Senior Data Analyst demonstrating Data Science to students, parents, and educators at a school Career Fair.

STRICT ANALYTICAL RULES:
1. Analyze ONLY the verified dataset statistics and records provided below. Never invent numbers, columns, or observations.
2. Support every claim with exact numerical evidence (averages, medians, Pearson r correlations, IQR outlier values, or regression slopes).
3. Explicitly distinguish CORRELATION from CAUSATION whenever discussing relationships or what-if scenarios.
4. Keep explanations clear, engaging, structured with short **bold** headings or bullet points, and easy to understand.
5. Briefly mention limitations (e.g., synthetic sample size, confounding variables) when drawing conclusions.

=== VERIFIED DATASET DOSSIER ===
$dossier

=== VISITOR QUESTION ===
$question"""
    }
}
