package com.mrsep.musicrecognizer.feature.preferences.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.feature.preferences.domain.MusicService
import kotlinx.collections.immutable.ImmutableSet
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun RequiredServicesDialog(
    modifier: Modifier = Modifier,
    requiredServices: ImmutableSet<MusicService>,
    onRequiredServicesChanged: (Set<MusicService>) -> Unit,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        title = {
            Text(text = stringResource(StringsR.string.music_services_links))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(StringsR.string.required_services_dialog),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                ) {
                    MusicService.entries.forEach { service ->
                        MusicServiceCheckbox(
                            service = service,
                            checked = requiredServices.contains(service),
                            onCheckedChange = { checked ->
                                onRequiredServicesChanged(
                                    requiredServices.run {
                                        if (checked) plus(service) else minus(service)
                                    }
                                )
                            }
                        )
                    }
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}

@Composable
private fun MusicServiceCheckbox(
    service: MusicService,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = service.getTitle(),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Stable
@Composable
internal fun MusicService.getTitle() = when (this) {
    MusicService.AmazonMusic -> stringResource(StringsR.string.amazon_music)
    MusicService.Anghami -> stringResource(StringsR.string.anghami)
    MusicService.AppleMusic -> stringResource(StringsR.string.apple_music)
    MusicService.Audiomack -> stringResource(StringsR.string.audiomack)
    MusicService.Audius -> stringResource(StringsR.string.audius)
    MusicService.Boomplay -> stringResource(StringsR.string.boomplay)
    MusicService.Deezer -> stringResource(StringsR.string.deezer)
    MusicService.MusicBrainz -> stringResource(StringsR.string.musicbrainz)
    MusicService.Napster -> stringResource(StringsR.string.napster)
    MusicService.Pandora -> stringResource(StringsR.string.pandora)
    MusicService.Soundcloud -> stringResource(StringsR.string.soundcloud)
    MusicService.Spotify -> stringResource(StringsR.string.spotify)
    MusicService.Tidal -> stringResource(StringsR.string.tidal)
    MusicService.YandexMusic -> stringResource(StringsR.string.yandex_music)
    MusicService.Youtube -> stringResource(StringsR.string.youtube)
    MusicService.YoutubeMusic -> stringResource(StringsR.string.youtubeMusic)
}