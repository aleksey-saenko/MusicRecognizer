package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
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
    val layoutDirection = LocalLayoutDirection.current
    Box(modifier = modifier) {
        LazyRow(
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            state = rememberLazyListState(),
            modifier = Modifier
                .rowFadingEdge(
                    startEdgeInitialColor = MaterialTheme.colorScheme.background,
                    fadeStartEdgeLengthDp = contentPadding.calculateStartPadding(layoutDirection),
                    fadeEndEdgeLengthDp = contentPadding.calculateEndPadding(layoutDirection)
                )
        ) {
            items(items = links) { link ->
                if (showOnlyIcons) {
                    MusicServiceIconCopy(
                        titleRes = link.type.titleId(),
                        iconRes = link.type.iconId(),
                        link = link.url
                    )
                } else {
                    MusicServiceChipCopy(
                        titleRes = link.type.titleId(),
                        iconRes = link.type.iconId(),
                        link = link.url
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MusicServiceChipCopy(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    link: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .sizeIn(minHeight = 32.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(0.75f),
                shape = MaterialTheme.shapes.small
            )
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onLongClick = { context.copyTextToClipboard(link) },
                onClick = { context.openUrlImplicitly(link) },
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            )
            .semantics { role = Role.Button }
            .padding(horizontal = 8.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MusicServiceIconCopy(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    link: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(0.75f),
                shape = CircleShape
            )
            .clip(CircleShape)
            .combinedClickable(
                onLongClick = { context.copyTextToClipboard(link) },
                onClick = { context.openUrlImplicitly(link) },
                indication = LocalIndication.current,
                interactionSource = remember { MutableInteractionSource() }
            )
            .semantics { role = Role.Button }
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = stringResource(titleRes),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun MusicServiceChipDefault(
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
private fun MusicServiceIconDefault(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    link: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    OutlinedIconButton(
        onClick = { context.openUrlImplicitly(link) },
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)
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

internal fun MusicService.titleId() = when (this) {
    MusicService.Spotify -> StringsR.string.spotify
    MusicService.Youtube -> StringsR.string.youtube
    MusicService.SoundCloud -> StringsR.string.soundcloud
    MusicService.AppleMusic -> StringsR.string.apple_music
    MusicService.Deezer -> StringsR.string.deezer
    MusicService.MusicBrainz -> StringsR.string.musicbrainz
    MusicService.Napster -> StringsR.string.napster
}

internal fun MusicService.iconId() = when (this) {
    MusicService.Spotify -> UiR.drawable.ic_spotify
    MusicService.Youtube -> UiR.drawable.ic_youtube
    MusicService.SoundCloud -> UiR.drawable.ic_soundcloud
    MusicService.AppleMusic -> UiR.drawable.ic_apple
    MusicService.Deezer -> UiR.drawable.ic_deezer
    MusicService.MusicBrainz -> UiR.drawable.ic_musicbrainz
    MusicService.Napster -> UiR.drawable.ic_napster
}