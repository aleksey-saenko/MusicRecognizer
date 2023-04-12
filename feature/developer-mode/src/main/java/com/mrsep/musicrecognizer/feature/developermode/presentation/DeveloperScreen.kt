package com.mrsep.musicrecognizer.feature.developermode.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
internal fun DeveloperScreen(
    viewModel: DeveloperViewModel = hiltViewModel()
) {
    DeveloperSection(
        modifier = Modifier.padding(16.dp),
        onRecordClickMR = { },
        onStopClickMR = { },
        onRecordClickAR = { },
        onStopClickAR = { },
        onPlayClickMP = { },
        onStopClickMP = { },
        onRecognizeClick = { },
        onFakeRecognizeClick = { },
        onClearDatabase = { },
        onPrepopulateDatabase = { }
    )
}