package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.feature.track.domain.model.MusicService
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ShareBottomSheet(
    track: TrackUi,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onShareClick: (String) -> Unit,
    onCopyClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier
    ) {
        var titleSelected by rememberSaveable(track.title) { mutableStateOf(true) }
        var artistSelected by rememberSaveable(track.artist) { mutableStateOf(true) }
        var albumSelected by rememberSaveable(track.album) { mutableStateOf(false) }
        var yearSelected by rememberSaveable(track.year) { mutableStateOf(false) }
        var selectedMusicServices by rememberSaveable(track.trackLinks) {
            mutableStateOf(setOf<MusicService>())
        }
        var lyricsSelected by rememberSaveable(track.year) { mutableStateOf(false) }

        fun buildStringToShare() = buildString {
            if (titleSelected) append(track.title)
            if (artistSelected) if (isNotBlank()) append(" - ${track.artist}") else append(track.artist)
            if (albumSelected) if (isNotBlank()) append(" - ${track.album}") else append(track.album)
            if (yearSelected) if (isNotBlank()) append(" (${track.year})") else append(track.year)
            if (lyricsSelected) if (isNotEmpty()) append("\n\n${track.lyrics}") else append(track.lyrics)
            track.trackLinks
                .filter { selectedMusicServices.contains(it.service) }
                .joinToString("\n") { it.url }
                .takeIf { serviceUrls -> serviceUrls.isNotBlank() }?.let { serviceUrls ->
                    if (isNotEmpty()) append("\n\n$serviceUrls") else append(serviceUrls)
                }
        }

        val shareAllowed = titleSelected || artistSelected || albumSelected ||
                yearSelected || selectedMusicServices.isNotEmpty() || lyricsSelected

        Text(
            text = stringResource(StringsR.string.share),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            ShareGroup(title = stringResource(StringsR.string.metadata)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    FilterChip(
                        selected = titleSelected,
                        onClick = { titleSelected = !titleSelected },
                        label = { Text(text = stringResource(StringsR.string.title)) }
                    )
                    FilterChip(
                        selected = artistSelected,
                        onClick = { artistSelected = !artistSelected },
                        label = { Text(text = stringResource(StringsR.string.artist)) }
                    )
                    track.album?.let {
                        FilterChip(
                            selected = albumSelected,
                            onClick = { albumSelected = !albumSelected },
                            label = { Text(text = stringResource(StringsR.string.album)) }
                        )
                    }
                    track.year?.let {
                        FilterChip(
                            selected = yearSelected,
                            onClick = { yearSelected = !yearSelected },
                            label = { Text(text = stringResource(StringsR.string.year)) }
                        )
                    }
                    track.lyrics?.let {
                        FilterChip(
                            selected = lyricsSelected,
                            onClick = { lyricsSelected = !lyricsSelected },
                            label = { Text(text = stringResource(StringsR.string.lyrics)) }
                        )
                    }
                }
            }
            if (track.trackLinks.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                ShareGroup(title = stringResource(StringsR.string.music_services_links)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        track.trackLinks.forEach { (_, serviceType) ->
                            val selected = selectedMusicServices.contains(serviceType)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedMusicServices = if (selected) {
                                        selectedMusicServices - serviceType
                                    } else {
                                        selectedMusicServices + serviceType
                                    }
                                },
                                label = { Text(text = stringResource(serviceType.titleId())) }
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            TextButton(
                onClick = { onCopyClick(buildStringToShare()) },
                enabled = shareAllowed
            ) {
                Text(
                    text = stringResource(StringsR.string.copy),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            TextButton(
                onClick = { onShareClick(buildStringToShare()) },
                enabled = shareAllowed
            ) {
                Text(
                    text = stringResource(StringsR.string.share),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun ShareGroup(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        content()
        Spacer(Modifier.height(4.dp))
    }
}
