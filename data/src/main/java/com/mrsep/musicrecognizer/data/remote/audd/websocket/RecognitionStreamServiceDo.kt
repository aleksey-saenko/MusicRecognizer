package com.mrsep.musicrecognizer.data.remote.audd.websocket

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import kotlinx.coroutines.flow.Flow

interface RecognitionStreamServiceDo {

    suspend fun recognize(
        token: String,
        audioRecordingFlow: Flow<ByteArray>
    ): RemoteRecognitionResultDo

}