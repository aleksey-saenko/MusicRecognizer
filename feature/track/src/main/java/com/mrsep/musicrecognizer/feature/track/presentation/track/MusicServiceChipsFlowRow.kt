package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.domain.track.model.MusicService
import com.mrsep.musicrecognizer.core.ui.components.VinylRotating
import com.mrsep.musicrecognizer.core.ui.modifiers.animatePlacement
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import kotlinx.collections.immutable.ImmutableList
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun MusicServiceChipsFlowRow(
    isLoading: Boolean,
    trackLinks: ImmutableList<TrackLink>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300)),
            modifier = Modifier
                .animatePlacement()
                .align(Alignment.CenterVertically)
        ) {
            key(Unit) {
                VinylRotating(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trackLinks.forEach { link ->
            key(link.service) {
                MusicServiceChip(
                    titleRes = link.service.titleId(),
                    iconRes = link.service.iconId(),
                    link = link.url,
                    modifier = Modifier.animatePlacement()
                )
            }
        }
    }
}

@Composable
internal fun MusicServiceChip(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    link: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        shadowElevation = 1.dp,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .sizeIn(minHeight = 32.dp)
                .combinedClickable(
                    interactionSource = null,
                    indication = LocalIndication.current,
                    onClick = { context.openUrlImplicitly(link) },
                    onLongClick = { context.copyTextToClipboard(link) },
                    role = Role.Button
                )
                .padding(horizontal = 8.dp)

        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                modifier = Modifier.size(SuggestionChipDefaults.IconSize)
            )
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
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

@Stable
internal fun MusicService.iconId() = when (this) {
    MusicService.AmazonMusic -> UiR.drawable.ic_amazon_24
    MusicService.Anghami -> UiR.drawable.ic_anghami_24
    MusicService.AppleMusic -> UiR.drawable.ic_apple_24
    MusicService.Audiomack -> UiR.drawable.ic_audiomack_24
    MusicService.Audius -> UiR.drawable.ic_audius_24
    MusicService.Boomplay -> UiR.drawable.ic_boomplay_24
    MusicService.Deezer -> UiR.drawable.ic_deezer_24
    MusicService.MusicBrainz -> UiR.drawable.ic_musicbrainz_24
    MusicService.Napster -> UiR.drawable.ic_napster_24
    MusicService.Pandora -> UiR.drawable.ic_pandora_24
    MusicService.Soundcloud -> UiR.drawable.ic_soundcloud_24
    MusicService.Spotify -> UiR.drawable.ic_spotify_24
    MusicService.Tidal -> UiR.drawable.ic_tidal_24
    MusicService.YandexMusic -> UiR.drawable.ic_yandex_music_24
    MusicService.Youtube -> UiR.drawable.ic_youtube_24
    MusicService.YoutubeMusic -> UiR.drawable.ic_youtube_music_24
}
