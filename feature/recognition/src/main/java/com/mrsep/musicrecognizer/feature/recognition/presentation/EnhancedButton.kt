package com.mrsep.musicrecognizer.feature.recognition.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun EnhancedButton(
    activated: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    longPressDelay: Long = 1_000,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val interaction by interactionSource.interactions.collectAsState(initial = null)
    var buttonLongPressed by remember { mutableStateOf(false) }
    val updatedOnClick by rememberUpdatedState(onClick)
    val updatedOnLongPress by rememberUpdatedState(onLongPress)

    LaunchedEffect(interaction, longPressDelay) {
        when (interaction) {
            is PressInteraction.Press -> {
                delay(longPressDelay)
                buttonLongPressed = true
                updatedOnLongPress()
            }
            is PressInteraction.Release -> {
                if (buttonLongPressed)
                    buttonLongPressed = false
                else
                    updatedOnClick()
            }
            is PressInteraction.Cancel -> {}
        }
    }
    Button(
        onClick = { },
        modifier = modifier,
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

@Composable
fun AnimatedEnhancedButton(
    activated: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val buttonSize = remember { Animatable(1f) }
    LaunchedEffect(activated) {
        buttonSize.animateTo(
            targetValue = 1.1f,
            animationSpec = SpringSpec(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        buttonSize.animateTo(
            targetValue = 1f,
            animationSpec = SpringSpec(
                dampingRatio = Spring.DampingRatioHighBouncy,
                stiffness = Spring.StiffnessVeryLow
            )
        )
    }
    EnhancedButton(
        modifier = modifier.scale(buttonSize.value),
        activated = activated,
        onClick = onClick,
        onLongPress = onLongPress,
        content = content,
    )
}