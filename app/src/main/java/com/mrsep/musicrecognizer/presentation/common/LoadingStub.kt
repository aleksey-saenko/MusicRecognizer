package com.mrsep.musicrecognizer.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

private const val DELAY_BEFORE_SHOW_INDICATOR_IN_MS = 500L

@Composable
fun LoadingStub(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        var delayPassed by rememberSaveable { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(DELAY_BEFORE_SHOW_INDICATOR_IN_MS)
            delayPassed = true
        }
        AnimatedVisibility(visible = delayPassed) {
            CircularProgressIndicator()
        }
    }
}