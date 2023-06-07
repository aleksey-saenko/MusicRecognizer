package com.mrsep.musicrecognizer.glue.recognitionqueue.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionStatusDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.RemoteRecognitionResult
import javax.inject.Inject

class EnqueuedStatusMapper @Inject constructor(
    private val remoteResultMapper: Mapper<RemoteRecognitionResultDo, RemoteRecognitionResult>
) :
    Mapper<EnqueuedRecognitionStatusDo, EnqueuedRecognitionStatus> {

    override fun map(input: EnqueuedRecognitionStatusDo): EnqueuedRecognitionStatus {
        return when (input) {
            EnqueuedRecognitionStatusDo.Inactive -> EnqueuedRecognitionStatus.Inactive
            EnqueuedRecognitionStatusDo.Enqueued -> EnqueuedRecognitionStatus.Enqueued
            EnqueuedRecognitionStatusDo.Running -> EnqueuedRecognitionStatus.Running
            is EnqueuedRecognitionStatusDo.Finished -> EnqueuedRecognitionStatus.Finished(
                remoteResultMapper.map(input.remoteResult)
            )
        }
    }

}