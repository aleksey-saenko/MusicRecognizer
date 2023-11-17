package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun RequiredServicesDialog(
    requiredServices: UserPreferences.RequiredServices,
    onRequiredServicesChanged: (UserPreferences.RequiredServices) -> Unit,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.music_services_links))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(StringsR.string.required_services_dialog),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = requiredServices.spotify,
                        onCheckedChange = { checked ->
                            onRequiredServicesChanged(
                                requiredServices.copy(spotify = checked)
                            )
                        }
                    )
                    Text(
                        text = stringResource(StringsR.string.spotify),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = requiredServices.youtube,
                        onCheckedChange = { checked ->
                            onRequiredServicesChanged(
                                requiredServices.copy(youtube = checked)
                            )
                        }
                    )
                    Text(
                        text = stringResource(StringsR.string.youtube),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = requiredServices.soundCloud,
                        onCheckedChange = { checked ->
                            onRequiredServicesChanged(
                                requiredServices.copy(soundCloud = checked)
                            )
                        }
                    )
                    Text(
                        text = stringResource(StringsR.string.soundcloud),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = requiredServices.appleMusic,
                        onCheckedChange = { checked ->
                            onRequiredServicesChanged(
                                requiredServices.copy(appleMusic = checked)
                            )
                        }
                    )
                    Text(
                        text = stringResource(StringsR.string.apple_music),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = requiredServices.deezer,
                        onCheckedChange = { checked ->
                            onRequiredServicesChanged(
                                requiredServices.copy(deezer = checked)
                            )
                        }
                    )
                    Text(
                        text = stringResource(StringsR.string.deezer),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = requiredServices.napster,
                        onCheckedChange = { checked ->
                            onRequiredServicesChanged(
                                requiredServices.copy(napster = checked)
                            )
                        }
                    )
                    Text(
                        text = stringResource(StringsR.string.napster),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = requiredServices.musicbrainz,
                        onCheckedChange = { checked ->
                            onRequiredServicesChanged(
                                requiredServices.copy(musicbrainz = checked)
                            )
                        }
                    )
                    Text(
                        text = stringResource(StringsR.string.musicbrainz),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}