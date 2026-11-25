package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.DatasetSpec
import com.example.ui.components.BadgePill
import com.example.ui.components.GlassCard
import com.example.ui.components.RichBoldText
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.VioletGlow
import com.example.viewmodel.AiCallState
import com.example.viewmodel.ChatMessage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiAnalystScreen(
    dataset: DatasetSpec,
    messages: List<ChatMessage>,
    aiState: AiCallState,
    onAskQuestion: (String) -> Unit,
    onRunLocalFallback: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val isBusy = aiState is AiCallState.Loading || aiState is AiCallState.Streaming

    LaunchedEffect(messages.size, aiState) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header + Suggested Questions
        GlassCard(contentPadding = PaddingValues(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = VioletGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "AI DATA ANALYST",
                            style = MaterialTheme.typography.labelMedium,
                            color = VioletGlow
                        )
                    }
                    Text(
                        text = "Ask Gemini about ${dataset.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate100
                    )
                }
                BadgePill(text = "Firebase AI Logic", accentColor = CyanGlow)
            }

            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dataset.suggestedQuestions.forEachIndexed { idx, q ->
                    Surface(
                        modifier = Modifier
                            .testTag("suggested_question_$idx")
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = !isBusy) { onAskQuestion(q) },
                        shape = RoundedCornerShape(10.dp),
                        color = Navy950.copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.28f))
                    ) {
                        Text(
                            text = q,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate200,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Chat Transcript
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(12.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(msg = msg)
                }

                if (aiState is AiCallState.Loading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = CyanGlow
                            )
                            Text(
                                text = "Analyzing ${dataset.name} distributions & correlations...",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyanGlow
                            )
                        }
                    }
                }

                if (aiState is AiCallState.Error) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = RoseError.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, RoseError.copy(alpha = 0.45f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = RoseError,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Gemini AI Request Failed",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = RoseError
                                    )
                                }
                                Text(
                                    text = aiState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate200
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onAskQuestion(aiState.lastQuestion) },
                                        border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.5f)),
                                        modifier = Modifier.testTag("retry_ai_button")
                                    ) {
                                        Text("Retry Gemini", color = CyanGlow)
                                    }
                                    OutlinedButton(
                                        onClick = { onRunLocalFallback(aiState.lastQuestion) },
                                        border = BorderStroke(1.dp, VioletGlow.copy(alpha = 0.5f)),
                                        modifier = Modifier.testTag("run_local_fallback_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Memory,
                                            contentDescription = null,
                                            tint = VioletGlow,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("Run Local Statistical Engine", color = Slate100)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Question Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        text = "Ask about ${dataset.name}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate400
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanGlow,
                    unfocusedBorderColor = Slate400.copy(alpha = 0.35f),
                    focusedContainerColor = Navy900,
                    unfocusedContainerColor = Navy900,
                    focusedTextColor = Slate100,
                    unfocusedTextColor = Slate100
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_question_input")
            )

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            if (inputText.isNotBlank() && !isBusy) listOf(CyanGlow, VioletGlow)
                            else listOf(Navy800, Navy800)
                        )
                    )
            ) {
                IconButton(
                    onClick = {
                        val q = inputText
                        inputText = ""
                        onAskQuestion(q)
                    },
                    enabled = inputText.isNotBlank() && !isBusy,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send question",
                        tint = if (inputText.isNotBlank() && !isBusy) Navy950 else Slate400
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!msg.isUser) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CyanGlow.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, CyanGlow.copy(alpha = 0.35f)),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "AI Analyst",
                    tint = CyanGlow,
                    modifier = Modifier
                        .padding(7.dp)
                        .size(16.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (msg.isUser) ElectricBlue.copy(alpha = 0.28f) else Navy950.copy(alpha = 0.85f),
            border = BorderStroke(
                1.dp,
                if (msg.isUser) VioletGlow.copy(alpha = 0.45f) else Slate400.copy(alpha = 0.2f)
            ),
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                RichBoldText(
                    text = msg.text,
                    baseColor = if (msg.isUser) Slate100 else Slate200
                )
                if (!msg.isUser && msg.sourceLabel.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = msg.sourceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate400
                    )
                }
            }
        }

        if (msg.isUser) {
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = VioletGlow.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, VioletGlow.copy(alpha = 0.4f)),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "You",
                    tint = VioletGlow,
                    modifier = Modifier
                        .padding(7.dp)
                        .size(16.dp)
                )
            }
        }
    }
}
