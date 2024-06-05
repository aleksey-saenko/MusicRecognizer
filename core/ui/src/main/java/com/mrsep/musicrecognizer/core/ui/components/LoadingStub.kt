package com.mrsep.musicrecognizer.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR

private const val AppearanceDelay = 1500

@Composable
fun LoadingStub(
    modifier: Modifier = Modifier,
    contentSize: Dp = 64.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        val visibleState = remember {
            MutableTransitionState(false).apply { targetState = true }
        }
        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(tween(delayMillis = AppearanceDelay)) +
                    scaleIn(tween(delayMillis = AppearanceDelay)),
            exit = fadeOut(tween(delayMillis = AppearanceDelay)) +
                    scaleOut(tween(delayMillis = AppearanceDelay)),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                VinylRotating(
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(contentSize)
                )
                Text(
                    text = stringResource(StringsR.string.loading),
                    modifier = Modifier.alpha(0.9f),
                )
            }
        }
    }
}
