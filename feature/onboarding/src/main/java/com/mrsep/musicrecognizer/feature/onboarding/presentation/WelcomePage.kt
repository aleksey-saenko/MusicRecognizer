package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun WelcomePage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(PaddingValues(horizontal = 24.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(StringsR.string.welcome),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(PaddingValues(vertical = 24.dp))
        )
//        AsyncImage(
//            model = R.drawable.sample_icon,
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.padding(bottom = 24.dp)
//        )
        Text(
            text = stringResource(StringsR.string.onboarding_welcome_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 488.dp)
                .padding(bottom = 24.dp)
        )
    }
}