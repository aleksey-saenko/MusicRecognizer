package com.mrsep.musicrecognizer.glue.recognitionqueue.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.RemoteRecognitionResult
import javax.inject.Inject

class EnqueuedStatusMapper @Inject constructor(
    private val remoteResultMapper: Mapper<RemoteRecognitionDataResult, RemoteRecognitionResult>
) :
    Mapper<EnqueuedRecognitionDataStatus, EnqueuedRecognitionStatus> {

    override fun map(input: EnqueuedRecognitionDataStatus): EnqueuedRecognitionStatus {
        return when (input) {
            EnqueuedRecognitionDataStatus.Inactive -> EnqueuedRecognitionStatus.Inactive
            EnqueuedRecognitionDataStatus.Enqueued -> EnqueuedRecognitionStatus.Enqueued
            EnqueuedRecognitionDataStatus.Running -> EnqueuedRecognitionStatus.Running
            is EnqueuedRecognitionDataStatus.Finished -> EnqueuedRecognitionStatus.Finished(
                remoteResultMapper.map(input.remoteResult)
            )
        }
    }

}