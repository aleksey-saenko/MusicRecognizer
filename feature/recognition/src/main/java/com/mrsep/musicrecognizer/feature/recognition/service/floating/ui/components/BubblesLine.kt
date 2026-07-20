package com.mrsep.musicrecognizer.feature.recognition.service.floating.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.LyricsPlayer
import kotlin.time.Duration

@Composable
internal fun BubblesLine(
    modifier: Modifier = Modifier,
    time: Duration,
    nextTime: Duration,
    textStyle: TextStyle,
    lyricsPlayer: LyricsPlayer,
) {
    val progressFraction by remember {
        derivedStateOf {
            ((lyricsPlayer.currentPosition - time) / (nextTime - time)).toFloat().coerceIn(0f, 1f)
        }
    }
    val firstBubbleProgress by remember {
        derivedStateOf { (progressFraction / 0.33f).coerceIn(0f, 1f) }
    }
    val secondBubbleProgress by remember {
        derivedStateOf { ((progressFraction - 0.33f) / 0.33f).coerceIn(0f, 1f) }
    }
    val thirdBubbleProgress by remember {
        derivedStateOf { ((progressFraction - 0.33f * 2) / 0.33f).coerceIn(0f, 1f) }
    }

    val density = LocalDensity.current
    val bubbleMaxSize = remember(density, textStyle) {
        val weightFactor = when (textStyle.fontWeight?.weight) {
            in 500..1000 -> 1.2f
            else -> 1f
        }
        with(density) { (textStyle.fontSize.toDp() * weightFactor * 0.35f).coerceAtLeast(4.dp) }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .height(bubbleMaxSize),
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(bubbleMaxSize * 0.75f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Bubble(
                modifier = Modifier.size(bubbleMaxSize),
                animationProgress = firstBubbleProgress
            )
            Bubble(
                modifier = Modifier.size(bubbleMaxSize),
                animationProgress = secondBubbleProgress
            )
            Bubble(
                modifier = Modifier.size(bubbleMaxSize),
                animationProgress = thirdBubbleProgress
            )
        }
    }
}

@Composable
private fun Bubble(
    modifier: Modifier = Modifier,
    animationProgress: Float,
    color: Color = LocalContentColor.current,
) {
    Canvas(modifier = modifier) {
        withTransform(
            transformBlock = {
                val scale = 0.5f + animationProgress / 2
                scale(scaleX = scale, scaleY = scale)
            }
        ) {
            drawCircle(
                color = color,
                alpha = 0.5f + animationProgress / 2
            )
        }
    }
}
