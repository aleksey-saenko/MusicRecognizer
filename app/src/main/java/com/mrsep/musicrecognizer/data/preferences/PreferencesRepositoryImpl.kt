package com.mrsep.musicrecognizer.data.preferences

import android.util.Log
import androidx.datastore.core.DataStore
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.VisibleLinksProto
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import com.mrsep.musicrecognizer.domain.model.Mapper
import com.mrsep.musicrecognizer.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PreferencesRepositoryImpl"

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferencesProto>,
    private val preferencesToDomainMapper: Mapper<UserPreferencesProto, UserPreferences>,
    private val visibleLinksToProtoMapper: Mapper<UserPreferences.VisibleLinks, VisibleLinksProto>
) : PreferencesRepository {

    private fun Flow<UserPreferencesProto>.ioExceptionCatcherOnRead(): Flow<UserPreferencesProto> {
        return this.catch { e ->
            Log.e(TAG, "Failed to read user preferences", e)
            when (e) {
                is IOException -> emit(UserPreferencesProto.getDefaultInstance())
                else -> throw e
            }
        }
    }

    private suspend fun safeWriter(
        action: UserPreferencesProto.Builder.() -> UserPreferencesProto.Builder
    ) {
        try {
            dataStore.updateData { currentPreferences ->
                currentPreferences.toBuilder()
                    .action()
                    .build()
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to update user preferences", e)
        }
    }

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .ioExceptionCatcherOnRead()
        .map { preferencesProto -> preferencesToDomainMapper.map(preferencesProto) }

    override suspend fun saveApiToken(newToken: String) {
        safeWriter { setApiToken(newToken) }
    }

    override suspend fun setOnboardingCompleted(value: Boolean) {
        safeWriter { setOnboardingCompleted(value) }
    }

    override suspend fun setNotificationServiceEnabled(value: Boolean) {
        safeWriter { setNotificationServiceEnabled(value) }
    }


    override suspend fun setDynamicColorsEnabled(value: Boolean) {
        safeWriter { setDynamicColorsEnabled(value) }
    }

    override suspend fun setVisibleLinks(visibleLinks: UserPreferences.VisibleLinks) {
        safeWriter { setVisibleLinks(visibleLinksToProtoMapper.map(visibleLinks)) }
    }

}

// DEPRECATED MAPPERS-EXTENSIONS (TO DELETE)
//private fun UserPreferencesProto.toDomain(): UserPreferences {
//    return UserPreferences(
//        onboardingCompleted = this.onboardingCompleted,
//        apiToken = this.apiToken,
//        visibleLinks = UserPreferences.VisibleLinks(
//            spotify = this.visibleLinks.spotify,
//            appleMusic = this.visibleLinks.appleMusic,
//            deezer = this.visibleLinks.deezer,
//            napster = this.visibleLinks.napster,
//            musicbrainz = this.visibleLinks.musicbrainz
//        )
//    )
//}
//
//private fun UserPreferences.VisibleLinks.toProto(): VisibleLinksProto {
//    return VisibleLinksProto.newBuilder()
//        .setSpotify(this.spotify)
//        .setAppleMusic(this.appleMusic)
//        .setDeezer(this.deezer)
//        .setNapster(this.napster)
//        .setMusicbrainz(this.musicbrainz)
//        .build()
//}