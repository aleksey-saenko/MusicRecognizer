package com.mrsep.musicrecognizer.data.remote.enhancer.odesli

import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import com.mrsep.musicrecognizer.data.track.TrackLinkDo

internal fun OdesliResponseJson.toTrackLinks(): List<TrackLinkDo> {
    return linksByPlatform?.run {
        listOfNotNull(
            amazonMusic?.url?.run { TrackLinkDo(this, MusicServiceDo.AmazonMusic) },
            anghami?.url?.run { TrackLinkDo(this, MusicServiceDo.Anghami) },
            appleMusic?.url?.run { TrackLinkDo(this, MusicServiceDo.AppleMusic) },
            audiomack?.url?.run { TrackLinkDo(this, MusicServiceDo.Audiomack) },
            audius?.url?.run { TrackLinkDo(this, MusicServiceDo.Audius) },
            boomplay?.url?.run { TrackLinkDo(this, MusicServiceDo.Boomplay) },
            deezer?.url?.run { TrackLinkDo(this, MusicServiceDo.Deezer) },
            napster?.url?.run { TrackLinkDo(this, MusicServiceDo.Napster) },
            pandora?.url?.run { TrackLinkDo(this, MusicServiceDo.Pandora) },
            soundcloud?.url?.run { TrackLinkDo(this, MusicServiceDo.Soundcloud) },
            spotify?.url?.run { TrackLinkDo(this, MusicServiceDo.Spotify) },
            tidal?.url?.run { TrackLinkDo(this, MusicServiceDo.Tidal) },
            yandex?.url?.run { TrackLinkDo(this, MusicServiceDo.YandexMusic) },
            youtube?.url?.run { TrackLinkDo(this, MusicServiceDo.Youtube) },
            youtubeMusic?.url?.run { TrackLinkDo(this, MusicServiceDo.YoutubeMusic) },
        )
    } ?: emptyList()
}