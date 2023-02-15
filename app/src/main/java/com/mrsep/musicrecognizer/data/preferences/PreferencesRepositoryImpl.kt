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
): PreferencesRepository {

    private fun Flow<UserPreferencesProto>.ioExceptionCatcherOnWrite(): Flow<UserPreferencesProto> {
        return this.catch { e ->
            Log.e(TAG, "Failed to read user preferences", e)
            when (e) {
                is IOException -> emit(UserPreferencesProto.getDefaultInstance())
                else -> throw e
            }
        }
    }

    private suspend fun ioExceptionCatcherOnRead(block: suspend () -> Unit) {
        runCatching {
            block.invoke()
        }.onFailure { e ->
            when (e) {
                is IOException -> Log.e(TAG, "Failed to update user preferences", e)
                else -> throw e
            }
        }
    }

    override val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .ioExceptionCatcherOnWrite()
        .map { preferencesProto -> preferencesToDomainMapper.map(preferencesProto) }

    override suspend fun saveApiToken(newToken: String) {
        ioExceptionCatcherOnRead {
            dataStore.updateData { currentPreferences ->
                currentPreferences.toBuilder()
                    .setApiToken(newToken)
                    .build()
            }
        }
    }

    override suspend fun setOnboardingCompleted(onboardingCompleted: Boolean) {
        ioExceptionCatcherOnRead {
            dataStore.updateData { currentPreferences ->
                currentPreferences.toBuilder()
                    .setOnboardingCompleted(onboardingCompleted)
                    .build()
            }
        }
    }

    override suspend fun setVisibleLinks(visibleLinks: UserPreferences.VisibleLinks) {
        ioExceptionCatcherOnRead {
            dataStore.updateData { currentPreferences ->
                currentPreferences.toBuilder()
                    .setVisibleLinks(visibleLinksToProtoMapper.map(visibleLinks))
                    .build()
            }
        }
    }

}

// DEPRECATED (TO DELETE)
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