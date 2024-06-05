package com.mrsep.musicrecognizer.data.audiorecord

import kotlinx.coroutines.flow.Flow

interface AudioRecordingControllerDo {

    fun audioRecordingFlow(scheme: RecognitionSchemeDo): Flow<Result<ByteArray>>
}
