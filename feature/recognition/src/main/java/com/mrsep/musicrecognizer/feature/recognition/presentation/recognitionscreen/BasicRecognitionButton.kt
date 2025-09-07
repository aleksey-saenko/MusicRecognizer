package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun BasicRecognitionButton(
    activated: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    longPressDelay: Long = 400,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val interaction by interactionSource.interactions.collectAsState(initial = null)
    var buttonLongPressed by remember { mutableStateOf(false) }
    val updatedOnClick by rememberUpdatedState(onClick)
    val updatedOnLongClick by rememberUpdatedState(onLongClick)

    LaunchedEffect(interaction, longPressDelay) {
        when (interaction) {
            is PressInteraction.Press -> {
                delay(longPressDelay)
                buttonLongPressed = true
                updatedOnLongClick()
            }

            is PressInteraction.Release -> {
                if (buttonLongPressed) {
                    buttonLongPressed = false
                } else {
                    updatedOnClick()
                }
            }

            is PressInteraction.Cancel -> {}
        }
    }

    val buttonScaleFactor = remember { Animatable(1f) }
    LaunchedEffect(activated) {
        buttonScaleFactor.animateTo(
            targetValue = 1.1f,
            animationSpec = SpringSpec(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        buttonScaleFactor.animateTo(
            targetValue = 1f,
            animationSpec = SpringSpec(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessVeryLow
            )
        )
    }

    Button(
        onClick = { },
        modifier = modifier.graphicsLayer {
            scaleX = buttonScaleFactor.value
            scaleY = buttonScaleFactor.value
        },
        enabled = true,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = Color.Unspecified,
            disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContentColor = Color.Unspecified,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 6.dp,
            focusedElevation = 6.dp,
            hoveredElevation = 8.dp,
            disabledElevation = 0.dp
        ),
        border = null,
        contentPadding = ButtonDefaults.ContentPadding,
        interactionSource = interactionSource,
        content = content
    )
}
