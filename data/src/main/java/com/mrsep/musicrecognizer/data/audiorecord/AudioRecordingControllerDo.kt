package com.mrsep.musicrecognizer.data.audiorecord

import kotlinx.coroutines.flow.Flow

interface AudioRecordingControllerDo {

    fun audioRecordingFlow(strategy: AudioRecordingStrategyDo): Flow<Result<ByteArray>>

}
