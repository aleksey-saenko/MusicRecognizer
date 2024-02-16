package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.animationDurationButton
import com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.animationDurationShield
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun AnimatedVisibilityScope.BaseShield(
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface {
        Box {
            FilledTonalIconButton(
                onClick = onDismissClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .zIndex(1f)
                    .animateEnterExit(
                        enter = slideInHorizontally(
                            animationSpec = tween(
                                durationMillis = animationDurationShield,
                                delayMillis = animationDurationButton
                            ),
                            initialOffsetX = { fullWidth -> -fullWidth }
                        ),
                        exit = slideOutHorizontally(
                            animationSpec = tween(
                                durationMillis = animationDurationShield,
                                delayMillis = 0
                            ),
                            targetOffsetX = { fullWidth -> -fullWidth }
                        )
                    )
            ) {
                Icon(
                    painter = painterResource(UiR.drawable.outline_close_24),
                    contentDescription = stringResource(StringsR.string.close),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .animateEnterExit(
                        enter = scaleIn(
                            initialScale = 0.8f,
                            animationSpec = tween(
                                durationMillis = animationDurationShield,
                                delayMillis = animationDurationButton
                            )
                        ),
                        exit = scaleOut(
                            targetScale = 0.8f,
                            animationSpec = tween(
                                durationMillis = animationDurationShield,
                                delayMillis = 0
                            )
                        )
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                content()
            }
        }
    }
}