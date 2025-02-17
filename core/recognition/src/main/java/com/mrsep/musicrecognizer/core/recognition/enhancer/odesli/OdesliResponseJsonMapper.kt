package com.mrsep.musicrecognizer.core.recognition.enhancer.odesli

import com.mrsep.musicrecognizer.core.domain.track.model.MusicService

internal fun OdesliResponseJson.toTrackLinks(): Map<MusicService, String> = linksByPlatform?.run {
    buildMap {
        amazonMusic?.url?.run { put(MusicService.AmazonMusic, this) }
        anghami?.url?.run { put(MusicService.Anghami, this) }
        appleMusic?.url?.run { put(MusicService.AppleMusic, this) }
        audiomack?.url?.run { put(MusicService.Audiomack, this) }
        audius?.url?.run { put(MusicService.Audius, this) }
        boomplay?.url?.run { put(MusicService.Boomplay, this) }
        deezer?.url?.run { put(MusicService.Deezer, this) }
        napster?.url?.run { put(MusicService.Napster, this) }
        pandora?.url?.run { put(MusicService.Pandora, this) }
        soundcloud?.url?.run { put(MusicService.Soundcloud, this) }
        spotify?.url?.run { put(MusicService.Spotify, this) }
        tidal?.url?.run { put(MusicService.Tidal, this) }
        yandex?.url?.run { put(MusicService.YandexMusic, this) }
        youtube?.url?.run { put(MusicService.Youtube, this) }
        youtubeMusic?.url?.run { put(MusicService.YoutubeMusic, this) }
    }
} ?: emptyMap()

internal fun OdesliResponseJson.toArtworkUrl(): String? {
    val sortedEntities = entitiesByUniqueId?.values
        ?.sortedBy { it.apiProvider?.ordinal }
    val hiRes = sortedEntities?.firstNotNullOfOrNull { entity ->
        entity.thumbnailUrl?.takeIf {
            entity.thumbnailHeight != null && entity.thumbnailWidth != null &&
                    entity.thumbnailHeight >= 500 && entity.thumbnailWidth >= 500
        }
    }
    return hiRes ?: sortedEntities?.firstNotNullOfOrNull { entity -> entity.thumbnailUrl }
}
