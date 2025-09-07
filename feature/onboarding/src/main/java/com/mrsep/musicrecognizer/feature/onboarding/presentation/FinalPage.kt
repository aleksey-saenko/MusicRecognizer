package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun FinalPage(
    modifier: Modifier = Modifier,
    onOnboardingCompleted: () -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(StringsR.string.onboarding_final_title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(StringsR.string.onboarding_final_message),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.widthIn(max = 488.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            modifier = Modifier.widthIn(min = 240.dp),
            onClick = onOnboardingCompleted
        ) {
            Text(text = stringResource(StringsR.string.onboarding_final_get_started))
        }
    }
}
