package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.core.strings.R as StringsR

internal class RequiredServicesDialogState(
    initialState: UserPreferences.RequiredServices,
) {
    var spotifyCheckBox by mutableStateOf(initialState.spotify)
    var youtubeCheckBox by mutableStateOf(initialState.youtube)
    var soundCloudCheckBox by mutableStateOf(initialState.soundCloud)
    var appleMusicCheckBox by mutableStateOf(initialState.appleMusic)
    var deezerCheckBox by mutableStateOf(initialState.deezer)
    var napsterCheckBox by mutableStateOf(initialState.napster)
    var musicBrainzCheckBox by mutableStateOf(initialState.musicbrainz)

    val currentState: UserPreferences.RequiredServices
        get() = UserPreferences.RequiredServices(
            spotify = spotifyCheckBox,
            youtube = youtubeCheckBox,
            soundCloud = soundCloudCheckBox,
            appleMusic = appleMusicCheckBox,
            deezer = deezerCheckBox,
            napster = napsterCheckBox,
            musicbrainz = musicBrainzCheckBox
        )

    companion object {
        val Saver: Saver<RequiredServicesDialogState, *> = listSaver(
            save = { listOf(
                it.spotifyCheckBox,
                it.youtubeCheckBox,
                it.soundCloudCheckBox,
                it.appleMusicCheckBox,
                it.deezerCheckBox,
                it.napsterCheckBox,
                it.musicBrainzCheckBox
            ) },
            restore = {
                RequiredServicesDialogState(
                    initialState = UserPreferences.RequiredServices(
                        spotify = it[0],
                        youtube = it[1],
                        soundCloud = it[2],
                        appleMusic = it[3],
                        deezer = it[4],
                        napster = it[5],
                        musicbrainz = it[6]
                    )
                )
            }
        )
    }

}

@Composable
internal fun rememberRequiredServicesDialogState(
    requiredServices: UserPreferences.RequiredServices,
): RequiredServicesDialogState {
    return rememberSaveable(inputs = arrayOf(requiredServices), saver = RequiredServicesDialogState.Saver) {
        RequiredServicesDialogState(
            initialState = requiredServices
        )
    }
}

@Composable
internal fun RequiredServicesDialog(
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    dialogState: RequiredServicesDialogState,
) {
    AlertDialog(
        title = {
            Text(text = stringResource(StringsR.string.music_services_links))
        },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(text = stringResource(StringsR.string.apply))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.cancel))
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
                        checked = dialogState.spotifyCheckBox,
                        onCheckedChange = { dialogState.spotifyCheckBox = it }
                    )
                    Text(
                        text = stringResource(StringsR.string.spotify),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dialogState.youtubeCheckBox,
                        onCheckedChange = { dialogState.youtubeCheckBox = it }
                    )
                    Text(
                        text = stringResource(StringsR.string.youtube),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dialogState.soundCloudCheckBox,
                        onCheckedChange = { dialogState.soundCloudCheckBox = it }
                    )
                    Text(
                        text = stringResource(StringsR.string.soundcloud),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dialogState.appleMusicCheckBox,
                        onCheckedChange = { dialogState.appleMusicCheckBox = it }
                    )
                    Text(
                        text = stringResource(StringsR.string.apple_music),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dialogState.deezerCheckBox,
                        onCheckedChange = { dialogState.deezerCheckBox = it }
                    )
                    Text(
                        text = stringResource(StringsR.string.deezer),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dialogState.napsterCheckBox,
                        onCheckedChange = { dialogState.napsterCheckBox = it }
                    )
                    Text(
                        text = stringResource(StringsR.string.napster),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dialogState.musicBrainzCheckBox,
                        onCheckedChange = { dialogState.musicBrainzCheckBox = it }
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