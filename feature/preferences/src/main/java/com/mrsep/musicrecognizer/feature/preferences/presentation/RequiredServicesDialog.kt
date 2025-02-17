package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import kotlin.random.Random
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun RequiredServicesDialog(
    modifier: Modifier = Modifier,
    requiredServices: List<MusicService>,
    onRequiredServicesChanged: (List<MusicService>) -> Unit,
    onDismissClick: () -> Unit
) {
    val fullList by remember(requiredServices) {
        derivedStateOf {
            val requiredServicesSet = requiredServices.toSet()
            val unselectedServices = MusicService.entries
                .filter { service -> !requiredServicesSet.contains(service) }
                .map { service -> MusicServiceUiState(service, false) }
            val selectedServices = requiredServices
                .map { service -> MusicServiceUiState(service, true) }
            selectedServices + unselectedServices
        }
    }
    AlertDialog(
        modifier = modifier,
        title = {
            Text(text = stringResource(StringsR.string.pref_title_music_services_links))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        dismissButton = {
            TextButton(onClick = { onRequiredServicesChanged(emptyList()) }) {
                Text(text = stringResource(StringsR.string.reset))
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(StringsR.string.music_services_links_dialog),
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    // Workaround for first element https://issuetracker.google.com/issues/209652366
                    itemsIndexed(
                        items = fullList,
                        key = { index, (service, _) -> if (index == 0) Random.nextInt() else service }
                    ) { _, (service, selected) ->
                        MusicServiceCheckbox(
                            service = service,
                            checked = selected,
                            onCheckedChange = {
                                onRequiredServicesChanged(
                                    requiredServices.run {
                                        if (selected) minus(service) else plus(service)
                                    }
                                )
                            },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}

@Immutable
private data class MusicServiceUiState(
    val service: MusicService,
    val selected: Boolean
)

@Composable
private fun MusicServiceCheckbox(
    service: MusicService,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
            Text(
                text = stringResource(service.titleId()),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Stable
internal fun MusicService.titleId() = when (this) {
    MusicService.AmazonMusic -> StringsR.string.amazon_music
    MusicService.Anghami -> StringsR.string.anghami
    MusicService.AppleMusic -> StringsR.string.apple_music
    MusicService.Audiomack -> StringsR.string.audiomack
    MusicService.Audius -> StringsR.string.audius
    MusicService.Boomplay -> StringsR.string.boomplay
    MusicService.Deezer -> StringsR.string.deezer
    MusicService.MusicBrainz -> StringsR.string.musicbrainz
    MusicService.Napster -> StringsR.string.napster
    MusicService.Pandora -> StringsR.string.pandora
    MusicService.Soundcloud -> StringsR.string.soundcloud
    MusicService.Spotify -> StringsR.string.spotify
    MusicService.Tidal -> StringsR.string.tidal
    MusicService.YandexMusic -> StringsR.string.yandex_music
    MusicService.Youtube -> StringsR.string.youtube
    MusicService.YoutubeMusic -> StringsR.string.youtubeMusic
}
