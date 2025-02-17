package com.mrsep.musicrecognizer.core.datastore

import androidx.datastore.core.DataMigration

internal object RequiredMusicServicesMigration : DataMigration<UserPreferencesProto> {

    override suspend fun shouldMigrate(currentData: UserPreferencesProto) =
        !currentData.hasDoneRequiredMusicServicesMigration

    override suspend fun migrate(currentData: UserPreferencesProto) = currentData.copy {
        requiredMusicServices.clear()
        requiredMusicServices.addAll(
            with(deprecatedRequiredServices) {
                listOfNotNull(
                    MusicServiceProto.Spotify.takeIf { spotify },
                    MusicServiceProto.Youtube.takeIf { youtube },
                    MusicServiceProto.Soundcloud.takeIf { soundcloud },
                    MusicServiceProto.AppleMusic.takeIf { appleMusic },
                    MusicServiceProto.Deezer.takeIf { deezer },
                    MusicServiceProto.Napster.takeIf { napster },
                    MusicServiceProto.MusicBrainz.takeIf { musicbrainz }
                )
            }
        )
        hasDoneRequiredMusicServicesMigration = true
    }

    override suspend fun cleanUp() = Unit
}
