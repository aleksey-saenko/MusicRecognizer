package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun TrackExtrasDialog(
    lastRecognitionDate: String,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        title = {
            Text(text = stringResource(StringsR.string.track_metainfo))
        },
        text = {
            Text(
                text = stringResource(StringsR.string.date_of_last_recognition, lastRecognitionDate)
            )
        }
    )
}