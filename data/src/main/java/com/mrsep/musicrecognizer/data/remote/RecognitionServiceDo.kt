package com.mrsep.musicrecognizer.data.remote

import kotlinx.coroutines.flow.Flow
import java.io.File
import java.net.URL

interface RecognitionServiceDo {

    suspend fun recognize(recording: ByteArray): RemoteRecognitionResultDo

    suspend fun recognize(recording: File): RemoteRecognitionResultDo

    suspend fun recognize(url: URL): RemoteRecognitionResultDo

    suspend fun recognize(recordingFlow: Flow<ByteArray>): RemoteRecognitionResultDo
}
