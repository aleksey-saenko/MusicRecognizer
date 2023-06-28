package com.mrsep.musicrecognizer.data.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.data.BuildConfig
import java.io.InputStream
import java.io.OutputStream

object UserPreferencesProtoSerializer : Serializer<UserPreferencesProto> {

    override val defaultValue: UserPreferencesProto
        get() = UserPreferencesProto.newBuilder()
            .setRequiredServices(
                UserPreferencesProto.RequiredServicesProto.newBuilder()
                    .setSpotify(true)
                    .setYoutube(true)
                    .setSoundcloud(true)
                    .setAppleMusic(true)
                    .setDeezer(true)
                    .setMusicbrainz(true)
                    .setNapster(true)
                    .build()
            )
            .setApiToken(BuildConfig.AUDD_TOKEN)
            .setOnboardingCompleted(true)
            .setNotificationServiceEnabled(false)
            .setDeveloperModeEnabled(true)
            .setSchedulePolicy(
                UserPreferencesProto.SchedulePolicyProto.newBuilder()
                    .setNoMatches(UserPreferencesProto.ScheduleActionProto.IGNORE)
                    .setBadConnection(UserPreferencesProto.ScheduleActionProto.SAVE_AND_LAUNCH)
                    .setAnotherFailure(UserPreferencesProto.ScheduleActionProto.SAVE)
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
