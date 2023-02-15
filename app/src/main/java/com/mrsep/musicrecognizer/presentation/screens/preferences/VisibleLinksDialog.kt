package com.mrsep.musicrecognizer.presentation.screens.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.domain.model.UserPreferences

class VisibleLinksDialogState(
    initialState: UserPreferences.VisibleLinks,
) {
    var spotifyCheckBox by mutableStateOf(initialState.spotify)
    var appleMusicCheckBox by mutableStateOf(initialState.appleMusic)
    var deezerCheckBox by mutableStateOf(initialState.deezer)
    var napsterCheckBox by mutableStateOf(initialState.napster)
    var musicBrainzCheckBox by mutableStateOf(initialState.musicbrainz)

    val currentState: UserPreferences.VisibleLinks
        get() = UserPreferences.VisibleLinks(
            spotify = spotifyCheckBox,
            appleMusic = appleMusicCheckBox,
            deezer = deezerCheckBox,
            napster = napsterCheckBox,
            musicbrainz = musicBrainzCheckBox
        )

    companion object {
        val Saver: Saver<VisibleLinksDialogState, *> = listSaver(
            save = { listOf(
                it.spotifyCheckBox,
                it.appleMusicCheckBox,
                it.deezerCheckBox,
                it.napsterCheckBox,
                it.musicBrainzCheckBox
            ) },
            restore = {
                VisibleLinksDialogState(
                    initialState = UserPreferences.VisibleLinks(
                        spotify = it[0],
                        appleMusic = it[1],
                        deezer = it[2],
                        napster = it[3],
                        musicbrainz = it[4]
                    )
                )
            }
        )
    }

}

@Composable
fun rememberVisibleLinksDialogState(
    visibleLinks: UserPreferences.VisibleLinks,
) = rememberSaveable(saver = VisibleLinksDialogState.Saver) {
    VisibleLinksDialogState(
        initialState = visibleLinks
    )
}

@Composable
fun VisibleLinksDialog(
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    dialogState: VisibleLinksDialogState,
) {
    AlertDialog(
        title = {
            Text(text = "Show links to")
        },
        confirmButton = {
            Button(onClick = { onConfirmClick() }) {
                Text(text = stringResource(R.string.apply))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismissClick() }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.spotify), modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = dialogState.spotifyCheckBox,
                        onCheckedChange = { dialogState.spotifyCheckBox = it }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.apple_music), modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = dialogState.appleMusicCheckBox,
                        onCheckedChange = { dialogState.appleMusicCheckBox = it }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.deezer), modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = dialogState.deezerCheckBox,
                        onCheckedChange = { dialogState.deezerCheckBox = it }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.napster), modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = dialogState.napsterCheckBox,
                        onCheckedChange = { dialogState.napsterCheckBox = it }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = stringResource(R.string.musicbrainz), modifier = Modifier.weight(1f))
                    Checkbox(
                        checked = dialogState.musicBrainzCheckBox,
                        onCheckedChange = { dialogState.musicBrainzCheckBox = it }
                    )
                }
            }
        },
        onDismissRequest = { }
    )
}