package com.mrsep.musicrecognizer.data.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.mrsep.musicrecognizer.UserPreferencesProto
import java.io.InputStream
import java.io.OutputStream

object UserPreferencesProtoSerializer : Serializer<UserPreferencesProto> {

    override val defaultValue: UserPreferencesProto
        get() = UserPreferencesProto.getDefaultInstance()
            .toBuilder()
            .setVisibleLinks(
                UserPreferencesProto.VisibleLinksProto.getDefaultInstance().toBuilder()
                    .setSpotify(true)
                    .setAppleMusic(true)
                    .setDeezer(true)
                    .setMusicbrainz(true)
                    .setNapster(true)
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
