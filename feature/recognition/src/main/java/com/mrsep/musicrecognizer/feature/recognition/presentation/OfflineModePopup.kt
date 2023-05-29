package com.mrsep.musicrecognizer.feature.recognition.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun OfflineModePopup(
    modifier: Modifier = Modifier,
    visible: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
            shape = CircleShape,
        ) {
            Text(
                text = stringResource(R.string.offline_mode),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(24.dp)
            )
        }

    }
}