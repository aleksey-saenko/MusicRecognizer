package com.mrsep.musicrecognizer.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SuperButton(
    activated: Boolean,
    amplitudeFactor: Float,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        RippleAnimated(
            activated = activated,
            amplitudeFactor = amplitudeFactor,
            modifier = Modifier.size(220.dp),
            baseColor = MaterialTheme.colorScheme.primary,
            activatedColor = MaterialTheme.colorScheme.primary,
            circlesCount = 9,
            animationSpeed = 12_000
        )
        AnimatedEnhancedButton(
            modifier = Modifier.size(150.dp),
            activated = activated,
            enabled = enabled,
            onClick = onClick,
            onLongPress = onLongPress
        ) {
            WaveAnimated(
                activated = activated,
                amplitudeFactor = amplitudeFactor,
                baseColor = MaterialTheme.colorScheme.secondary,
                activatedColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SuperButtonSection(
    title: String,
    activated: Boolean,
    amplitudeFactor: Float,
    enabled: Boolean,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedContent(
            contentAlignment = Alignment.Center,
            targetState = title,
            transitionSpec = {
                slideIntoContainer(
                    towards = AnimatedContentScope.SlideDirection.Up,
                    animationSpec = tween(durationMillis = 250, delayMillis = 90)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 200, delayMillis = 90)
                ) with slideOutOfContainer(
                    towards = AnimatedContentScope.SlideDirection.Up,
                    animationSpec = tween(durationMillis = 250, delayMillis = 90)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 200, delayMillis = 90)
                )
            }
        ) {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }

        SuperButton(
            onClick = onButtonClick,
            onLongPress = onButtonClick,
            activated = activated,
            amplitudeFactor = amplitudeFactor,
            enabled = enabled
        )
    }
}