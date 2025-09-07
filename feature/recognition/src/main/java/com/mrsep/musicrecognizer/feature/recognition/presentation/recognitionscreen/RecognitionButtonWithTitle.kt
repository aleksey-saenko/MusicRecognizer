package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

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
internal fun RecognitionButtonWithTitle(
    title: String,
    activated: Boolean,
    soundLevelState: State<Float>,
    onButtonClick: () -> Unit,
    onButtonLongClick: () -> Unit,
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
                (slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(durationMillis = 350)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 300, easing = EaseOut)
                )).togetherWith(
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(durationMillis = 350)
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 150, easing = EaseIn)
                    )
                )
            },
            label = "buttonTitleTransition"
        ) {
            Text(
                text = it,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp)
            )
        }

        RecognitionButton(
            onClick = onButtonClick,
            onLongClick = onButtonLongClick,
            activated = activated,
            soundLevelState = soundLevelState,
        )
    }
}

@Composable
private fun RecognitionButton(
    activated: Boolean,
    soundLevelState: State<Float>,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        RippleAnimated(
            activated = activated,
            modifier = Modifier.size(256.dp),
            startOffset = 0.5f, // Should be a bit less than buttonSize/rippleSize
            baseColor = MaterialTheme.colorScheme.primary,
            activatedColor = MaterialTheme.colorScheme.primary,
            circlesCount = 7,
            animationSpeed = 12_000
        )
        BasicRecognitionButton(
            modifier = Modifier
                .size(176.dp),
            activated = activated,
            onClick = onClick,
            onLongClick = onLongClick
        ) {
            WaveAnimated(
                activated = activated,
                soundLevelState = soundLevelState,
                properties = WaveAnimatedProperties(
                    baseColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                    activatedColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier
                    .fillMaxHeight(0.65f)
                    .fillMaxWidth(0.9f)
            )
        }
    }
}
