package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.VinylRotating
import com.mrsep.musicrecognizer.core.ui.modifiers.animatePlacement
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.feature.track.domain.model.MusicService
import com.mrsep.musicrecognizer.feature.track.domain.model.TrackLink
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalLayoutApi::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MusicServiceChip(
    @StringRes titleRes: Int,
    @DrawableRes iconRes: Int,
    link: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewConfiguration = LocalViewConfiguration.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(viewConfiguration.longPressTimeoutMillis)
            context.copyTextToClipboard(link)
        }
    }
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        ElevatedSuggestionChip(
            onClick = { context.openUrlImplicitly(link) },
            label = {
                Text(text = stringResource(titleRes))
            },
            icon = {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(SuggestionChipDefaults.IconSize)
                )
            },
            modifier = modifier,
            interactionSource = interactionSource
        )
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