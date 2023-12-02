package com.mrsep.musicrecognizer.data.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.mrsep.musicrecognizer.MusicServiceProto
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.data.BuildConfig
import java.io.InputStream
import java.io.OutputStream

object UserPreferencesProtoSerializer : Serializer<UserPreferencesProto> {

    override val defaultValue: UserPreferencesProto
        get() = UserPreferencesProto.newBuilder()
            .setOnboardingCompleted(false)
            .setApiToken(BuildConfig.AUDD_TOKEN)
            .addAllRequiredMusicServices(
                // add default audd services
                listOf(
                    MusicServiceProto.Spotify,
                    MusicServiceProto.Youtube,
                    MusicServiceProto.Soundcloud,
                    MusicServiceProto.AppleMusic,
                    MusicServiceProto.Deezer,
                    MusicServiceProto.MusicBrainz,
                    MusicServiceProto.Napster,
                )
            )
            .setDynamicColorsEnabled(true)
            .setArtworkBasedThemeEnabled(false)
            .setUseColumnForLibrary(false)
            .setNotificationServiceEnabled(false)
            .setDeveloperModeEnabled(true)
            .setFallbackPolicy(
                UserPreferencesProto.FallbackPolicyProto.newBuilder()
                    .setNoMatches(UserPreferencesProto.FallbackActionProto.IGNORE)
                    .setBadConnection(UserPreferencesProto.FallbackActionProto.SAVE_AND_LAUNCH)
                    .setAnotherFailure(UserPreferencesProto.FallbackActionProto.SAVE)
                    .build()
            )
            .setLyricsFontStyle(
                UserPreferencesProto.LyricsFontStyleProto.newBuilder()
                    .setFontSize(UserPreferencesProto.FontSizeProto.NORMAL)
                    .setIsBold(false)
                    .setIsHighContrast(false)
            )
            .setTrackFilter(
                UserPreferencesProto.TrackFilterProto.newBuilder()
                    .setFavoritesMode(UserPreferencesProto.FavoritesModeProto.ALL)
                    .setSortBy(UserPreferencesProto.SortByProto.RECOGNITION_DATE)
                    .setOrderBy(UserPreferencesProto.OrderByProto.DESC)
                    .setStartDate(Long.MIN_VALUE)
                    .setEndDate(Long.MAX_VALUE)
                    .build()
            )
            .setHapticFeedback(
                UserPreferencesProto.HapticFeedbackProto.newBuilder()
                    .setVibrateOnTap(false)
                    .setVibrateOnResult(false)
                    .build()
            )
            .setThemeMode(UserPreferencesProto.ThemeModeProto.FOLLOW_SYSTEM)
            .setUsePureBlackForDarkTheme(false)
            .setHasDoneRequiredMusicServicesMigration(true)
            .build()

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
