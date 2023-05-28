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
        get() = UserPreferencesProto.getDefaultInstance()
            .toBuilder()
            .setRequiredServices(
                UserPreferencesProto.RequiredServicesProto.getDefaultInstance().toBuilder()
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
            .setNotificationServiceEnabled(true)
            .setDeveloperModeEnabled(true)
            .setSchedulePolicy(
                UserPreferencesProto.SchedulePolicyProto.getDefaultInstance().toBuilder()
                    .setNoMatchesValue(UserPreferencesProto.ScheduleActionProto.IGNORE_VALUE)
                    .setBadConnectionValue(UserPreferencesProto.ScheduleActionProto.SAVE_AND_LAUNCH_VALUE)
                    .setAnotherFailureValue(UserPreferencesProto.ScheduleActionProto.SAVE_VALUE)
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
