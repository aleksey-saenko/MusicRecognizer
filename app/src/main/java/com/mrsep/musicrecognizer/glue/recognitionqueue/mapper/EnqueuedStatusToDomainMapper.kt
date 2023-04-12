package com.mrsep.musicrecognizer.glue.recognitionqueue.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionStatus
import javax.inject.Inject

class EnqueuedStatusToDomainMapper @Inject constructor() :
    Mapper<EnqueuedRecognitionDataStatus, EnqueuedRecognitionStatus> {

    override fun map(input: EnqueuedRecognitionDataStatus): EnqueuedRecognitionStatus {
        return when (input) {
            EnqueuedRecognitionDataStatus.Inactive -> EnqueuedRecognitionStatus.Inactive
            EnqueuedRecognitionDataStatus.Enqueued -> EnqueuedRecognitionStatus.Enqueued
            EnqueuedRecognitionDataStatus.Running -> EnqueuedRecognitionStatus.Running
            EnqueuedRecognitionDataStatus.Canceled -> EnqueuedRecognitionStatus.Canceled
            EnqueuedRecognitionDataStatus.Finished.NotFound -> EnqueuedRecognitionStatus.Finished.NotFound
            is EnqueuedRecognitionDataStatus.Finished.Error -> EnqueuedRecognitionStatus.Finished.Error(input.message)
            is EnqueuedRecognitionDataStatus.Finished.Success -> EnqueuedRecognitionStatus.Finished.Success(input.trackMbId)
        }
    }

}