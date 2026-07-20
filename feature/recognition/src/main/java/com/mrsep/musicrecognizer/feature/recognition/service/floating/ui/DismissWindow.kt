package com.mrsep.musicrecognizer.feature.recognition.service.floating.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.theme.MusicRecognizerTheme
import com.mrsep.musicrecognizer.feature.recognition.service.floating.DismissWindowState
import kotlinx.coroutines.flow.drop
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun DismissWindow(dismissWindowState: DismissWindowState) {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        snapshotFlow { dismissWindowState.isTargetMagnetized }
            .drop(1)
            .collect { isTargetMagnetized ->
                if (isTargetMagnetized) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                } else {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                }
            }
    }
    MusicRecognizerTheme(darkTheme = true) {
        val fadeBrush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            )
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .background(brush = fadeBrush)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                ,
                contentAlignment = Alignment.BottomCenter
            ) {
                val containerScale by animateFloatAsState(
                    targetValue = if (dismissWindowState.isTargetMagnetized) 0.8f else 1f,
                )
                val iconColor = MaterialTheme.colorScheme.onErrorContainer
                val containerColor  = MaterialTheme.colorScheme.errorContainer
                Box(
                    modifier = Modifier
                        .padding(bottom = 64.dp)
                        .size(80.dp)
                        .drawBehind {
                            drawCircle(
                                color = containerColor,
                                radius = size.minDimension / 2.0f * containerScale,
                            )
                        }
                        .onGloballyPositioned { coordinates ->
                            val position = coordinates.positionInWindow()
                            val size = coordinates.size

                            dismissWindowState.trashIconCenterX = position.x + size.width / 2f
                            dismissWindowState.trashIconCenterY = position.y + size.height / 2f

                            dismissWindowState.magnetRadius = size.width / 2f +
                                    with(density) { 32.dp.toPx() }
                            dismissWindowState.breakoutRadius = dismissWindowState.magnetRadius +
                                    with(density) { 100.dp.toPx() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(UiR.drawable.rounded_close_24),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
