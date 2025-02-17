package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.domain.recognition.model.RecognitionProvider
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun TrackExtrasDialog(
    track: TrackUi,
    onDismissClick: () -> Unit,
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                track.duration?.let {
                    InfoColumn(
                        title = stringResource(StringsR.string.track_info_title_duration),
                        subtitle = track.duration.toString()
                    )
                }
                track.recognizedAt?.let {
                    InfoColumn(
                        title = stringResource(StringsR.string.track_info_title_recognized_at),
                        subtitle = track.recognizedAt.toString()
                    )
                }
                InfoColumn(
                    title = stringResource(StringsR.string.track_info_title_recognized_by),
                    subtitle = track.recognizedBy.getTitle()
                )
                InfoColumn(
                    title = stringResource(StringsR.string.track_info_title_date_of_last_recognition),
                    subtitle = track.lastRecognitionDate
                )
            }
        }
    )
}

@Composable
private fun InfoColumn(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.height(2.dp))
        Text(text = subtitle)
    }
}

@Stable
@Composable
internal fun RecognitionProvider.getTitle() = when (this) {
    RecognitionProvider.Audd -> stringResource(StringsR.string.audd)
    RecognitionProvider.AcrCloud -> stringResource(StringsR.string.acr_cloud)
}
