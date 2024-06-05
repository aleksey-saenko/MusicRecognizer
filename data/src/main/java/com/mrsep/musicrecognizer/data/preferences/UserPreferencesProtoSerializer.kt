package com.mrsep.musicrecognizer.data.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.mrsep.musicrecognizer.MusicServiceProto
import com.mrsep.musicrecognizer.RecognitionProviderProto
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProtoKt.fallbackPolicyProto
import com.mrsep.musicrecognizer.UserPreferencesProtoKt.hapticFeedbackProto
import com.mrsep.musicrecognizer.UserPreferencesProtoKt.lyricsFontStyleProto
import com.mrsep.musicrecognizer.UserPreferencesProtoKt.trackFilterProto
import com.mrsep.musicrecognizer.acrCloudConfigProto
import com.mrsep.musicrecognizer.data.BuildConfig
import com.mrsep.musicrecognizer.userPreferencesProto
import java.io.InputStream
import java.io.OutputStream

object UserPreferencesProtoSerializer : Serializer<UserPreferencesProto> {

    override val defaultValue: UserPreferencesProto
        get() = userPreferencesProto {
            onboardingCompleted = false
            currentRecognitionProvider = RecognitionProviderProto.Audd
            apiToken = BuildConfig.AUDD_TOKEN
            acrCloudConfig = acrCloudConfigProto {
                host = BuildConfig.ACR_CLOUD_HOST
                accessKey = BuildConfig.ACR_CLOUD_ACCESS_KEY
                accessSecret = BuildConfig.ACR_CLOUD_ACCESS_SECRET
            }
            requiredMusicServices.addAll(
                // ordered by popularity
                listOf(
                    MusicServiceProto.Spotify,
                    MusicServiceProto.AppleMusic,
                    MusicServiceProto.Youtube,
                    MusicServiceProto.YoutubeMusic,
                    MusicServiceProto.AmazonMusic,
                    MusicServiceProto.Deezer,
                    MusicServiceProto.Tidal,
                    MusicServiceProto.Soundcloud,
                    MusicServiceProto.YandexMusic,
                    MusicServiceProto.Napster,
                    MusicServiceProto.Pandora,
                    MusicServiceProto.Anghami,
                    MusicServiceProto.Audiomack,
                    MusicServiceProto.Audius,
                    MusicServiceProto.Boomplay,
                    MusicServiceProto.MusicBrainz,
                )
            )
            notificationServiceEnabled = false
            dynamicColorsEnabled = true
            artworkBasedThemeEnabled = false
            useGridForLibrary = false
            useGridForRecognitionQueue = false
            showRecognitionDateInLibrary = false
            showCreationDateInQueue = false
            developerModeEnabled = false // was true in first public builds, reset for using
            fallbackPolicy = fallbackPolicyProto {
                noMatches = UserPreferencesProto.FallbackActionProto.IGNORE
                badConnection = UserPreferencesProto.FallbackActionProto.SAVE_AND_LAUNCH
                anotherFailure = UserPreferencesProto.FallbackActionProto.SAVE
            }
            lyricsFontStyle = lyricsFontStyleProto {
                fontSize = UserPreferencesProto.FontSizeProto.NORMAL
                isBold = false
                isHighContrast = false
                alignToStart = false
            }
            trackFilter = trackFilterProto {
                favoritesMode = UserPreferencesProto.FavoritesModeProto.ALL
                sortBy = UserPreferencesProto.SortByProto.RECOGNITION_DATE
                orderBy = UserPreferencesProto.OrderByProto.DESC
                startDate = Long.MIN_VALUE
                endDate = Long.MAX_VALUE
            }
            hapticFeedback = hapticFeedbackProto {
                vibrateOnTap = true
                vibrateOnResult = true
            }
            themeMode = UserPreferencesProto.ThemeModeProto.FOLLOW_SYSTEM
            usePureBlackForDarkTheme = false
            hasDoneRequiredMusicServicesMigration = true
            recognizeOnStartup = false
        }

    override suspend fun readFrom(input: InputStream): UserPreferencesProto {
        try {
            return UserPreferencesProto.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: UserPreferencesProto, output: OutputStream) {
        t.writeTo(output)
    }
}
