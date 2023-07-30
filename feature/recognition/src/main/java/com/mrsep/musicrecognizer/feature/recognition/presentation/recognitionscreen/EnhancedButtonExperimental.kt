package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

private enum class PressState { Released, Pressed }
private enum class ButtonEvent { Idle, Pressed, LongPressed, Clicked, Canceled }

@Composable
internal fun AnimatedEnhancedButtonExperimental(
    activated: Boolean,
    enabled: Boolean,
    pressedMode: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    longPressDelay: Long = 1_000,
    content: @Composable BoxScope.() -> Unit
) {
//    var buttonEvent by remember { mutableStateOf(ButtonEvent2.Idle) }
//    val updatedOnClick by rememberUpdatedState(onClick)
//    val updatedOnLongPress by rememberUpdatedState(onLongPress)

    val scaleFactor = remember { Animatable(1f) }
    var internalPressState by remember { mutableStateOf(PressState.Released) }

    LaunchedEffect(pressedMode) {
        println("pressedMode=$pressedMode")
        if (!pressedMode) {
            internalPressState = PressState.Released
        }
    }

    LaunchedEffect(internalPressState) {
        println("internalPressState=$internalPressState")
        when (internalPressState) {
            PressState.Pressed -> scaleFactor.animateTo(
                targetValue = 0.95f,
                animationSpec = tween()
            )
            PressState.Released -> scaleFactor.animateTo(
                targetValue = 1f,
                animationSpec = tween()
            )
        }
    }

//                scaleFactor.animateTo(
//                    targetValue = 1.1f,
//                    animationSpec = SpringSpec(
//                        dampingRatio = Spring.DampingRatioLowBouncy,
//                        stiffness = Spring.StiffnessLow
//                    )
//                )
//                scaleFactor.animateTo(
//                    targetValue = 1f,
//                    animationSpec = SpringSpec(
//                        dampingRatio = Spring.DampingRatioHighBouncy,
//                        stiffness = Spring.StiffnessVeryLow
//                    )
//                )

    ProvideLongPressTimeout {
        Box(
            modifier = modifier
                .scale(scaleFactor.value)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                )
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .semantics { role = Role.Button }
                .pointerInput(pressedMode) {
                    detectTapGestures(
                        onPress = {
                            if (!pressedMode) {
                                internalPressState = PressState.Pressed
                            }
                        },
                        onLongPress = {
                            onLongPress()
                        },
                        onTap = {
                            if (!pressedMode) {
                                internalPressState = PressState.Released
                                onClick()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center,
            content = content
        )
    }

}

@Composable
private fun ProvideLongPressTimeout(
    timeoutMs: Long = 2000L,
    content: @Composable () -> Unit
) {
    fun ViewConfiguration.withoutTouchSlop() = object : ViewConfiguration {
        override val longPressTimeoutMillis get() = timeoutMs

        override val doubleTapTimeoutMillis
            get() =
                this@withoutTouchSlop.doubleTapTimeoutMillis

        override val doubleTapMinTimeMillis
            get() =
                this@withoutTouchSlop.doubleTapMinTimeMillis

        override val touchSlop: Float
            get() =
                this@withoutTouchSlop.touchSlop
    }
    CompositionLocalProvider(
        LocalViewConfiguration provides LocalViewConfiguration.current.withoutTouchSlop()
    ) {
        content()
    }
}