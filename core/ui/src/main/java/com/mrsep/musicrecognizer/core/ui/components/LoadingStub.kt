package com.mrsep.musicrecognizer.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val DELAY_BEFORE_SHOW_INDICATOR_IN_MS = 1000L

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
        if (delayPassed) {
            VinylAnimated(
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}