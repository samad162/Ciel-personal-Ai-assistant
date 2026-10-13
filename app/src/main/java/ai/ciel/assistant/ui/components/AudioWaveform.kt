package ai.ciel.assistant.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import ai.ciel.assistant.domain.model.AssistantState
import ai.ciel.assistant.ui.theme.CielAccentCyan
import ai.ciel.assistant.ui.theme.CielNeonGlow

@Composable
fun AudioWaveform(
    state: AssistantState,
    rmsLevel: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .width(48.dp)
            .height(28.dp)
    ) {
        val barCount = 5
        val barWidth = 3.dp.toPx()
        val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)
        val centerY = size.height / 2

        for (i in 0 until barCount) {
            val x = i * (barWidth + spacing) + barWidth / 2
            val heightMultiplier = when (state) {
                AssistantState.LISTENING -> {
                    val sine = Math.sin(phase + (i * 0.9)).toFloat()
                    0.3f + (0.5f * (sine + 1f) / 2f) + (rmsLevel * 0.4f)
                }
                AssistantState.SPEAKING -> {
                    val sine = Math.sin(phase * 1.5 + (i * 1.2)).toFloat()
                    0.25f + (0.6f * (sine + 1f) / 2f)
                }
                AssistantState.PROCESSING -> {
                    val sine = Math.sin(phase * 2.0 + (i * 0.5)).toFloat()
                    0.2f + (0.4f * (sine + 1f) / 2f)
                }
                else -> 0.2f
            }

            val barHeight = (size.height * heightMultiplier).coerceIn(4f, size.height)
            val color = when (state) {
                AssistantState.LISTENING -> CielNeonGlow
                AssistantState.SPEAKING -> CielAccentCyan
                AssistantState.ERROR -> Color(0xFFEF4444)
                else -> Color(0xFF38BDF8).copy(alpha = 0.5f)
            }

            drawLine(
                color = color,
                start = Offset(x, centerY - barHeight / 2),
                end = Offset(x, centerY + barHeight / 2),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
