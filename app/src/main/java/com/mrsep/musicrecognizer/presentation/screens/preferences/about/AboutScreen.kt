package com.mrsep.musicrecognizer.presentation.screens.preferences.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "About",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(PaddingValues(vertical = 24.dp))
        )
        Text(
            text = "Powered by AudD®",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(PaddingValues(vertical = 24.dp))
        )
    }
}