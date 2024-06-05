package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun TrackExtrasDialog(
    track: TrackUi,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        title = {
            Text(text = stringResource(StringsR.string.track_info))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                track.duration?.let {
                    Text(
                        text = stringResource(StringsR.string.format_track_duration, track.duration)
                    )
                }
                track.recognizedAt?.let {
                    Text(
                        text = stringResource(StringsR.string.format_recognized_at, track.recognizedAt)
                    )
                }
                Text(
                    text = stringResource(StringsR.string.format_date_of_last_recognition, track.lastRecognitionDate)
                )
            }
        }
    )
}
