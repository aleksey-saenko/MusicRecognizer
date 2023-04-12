package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun FinalPage(
    modifier: Modifier = Modifier,
    onOnboardingCompletedClick: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier.padding(PaddingValues(horizontal = 24.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "We are ready to go",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(PaddingValues(vertical = 24.dp))
        )
        Text(
            text = "Setup completed successfully",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 488.dp)
                .padding(bottom = 24.dp)
        )
        Button(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .widthIn(min = 240.dp),
            onClick = {
                viewModel.setOnboardingCompleted(true)
                onOnboardingCompletedClick()
            }
        ) {
            Text(text = "Get started")
        }
    }
}