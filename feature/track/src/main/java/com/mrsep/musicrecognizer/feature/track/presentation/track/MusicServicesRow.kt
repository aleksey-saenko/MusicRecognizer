package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ServicesChipsLazyRow(
    links: ImmutableList<ServiceLink>,
    showOnlyIcons: Boolean,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberLazyListState()
    Box(modifier = modifier) {
        LazyRow(
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = scrollState,
            modifier = Modifier
                .rowFadingEdge(
                    startEdgeInitialColor = MaterialTheme.colorScheme.background,
                    isVisibleStartEdge = scrollState.canScrollBackward,
                    isVisibleEndEdge = scrollState.canScrollForward,
                )
        ) {
            items(items = links) { link ->
                if (showOnlyIcons) {
                    MusicServiceIcon(
                        titleRes = link.titleId(),
                        iconRes = link.iconId(),
                        link = link.url
                    )
                } else {
                    MusicServiceChip(
                        titleRes = link.titleId(),
                        iconRes = link.iconId(),
                        link = link.url
                    )
                }
            }
        }
    }
}

@Composable
private fun MusicServiceChip(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    link: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    SuggestionChip(
        onClick = { context.openUrlImplicitly(link) },
        label = {
            Text(text = stringResource(titleRes))
        },
        icon = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        border = SuggestionChipDefaults.suggestionChipBorder(
            MaterialTheme.colorScheme.outline.copy(
                0.75f
            )
        ),
        modifier = modifier
    )
}

@Composable
private fun MusicServiceIcon(
    @StringRes titleRes: Int, // add popup on long press
    @DrawableRes iconRes: Int,
    link: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    OutlinedIconButton(
        onClick = { context.openUrlImplicitly(link) },
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = stringResource(titleRes),
            modifier = Modifier.size(20.dp)
        )
    }
}

internal fun ServiceLink.titleId() = when (this) {
    is ServiceLink.Spotify -> StringsR.string.spotify
    is ServiceLink.Youtube -> StringsR.string.youtube
    is ServiceLink.SoundCloud -> StringsR.string.soundcloud
    is ServiceLink.AppleMusic -> StringsR.string.apple_music
    is ServiceLink.Deezer -> StringsR.string.deezer
    is ServiceLink.MusicBrainz -> StringsR.string.musicbrainz
    is ServiceLink.Napster -> StringsR.string.napster
}

internal fun ServiceLink.iconId() = when (this) {
    is ServiceLink.Spotify -> UiR.drawable.ic_spotify
    is ServiceLink.Youtube -> UiR.drawable.ic_youtube
    is ServiceLink.SoundCloud -> UiR.drawable.ic_soundcloud
    is ServiceLink.AppleMusic -> UiR.drawable.ic_apple
    is ServiceLink.Deezer -> UiR.drawable.ic_deezer
    is ServiceLink.MusicBrainz -> UiR.drawable.ic_musicbrainz
    is ServiceLink.Napster -> UiR.drawable.ic_napster
}