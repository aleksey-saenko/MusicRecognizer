package com.mrsep.musicrecognizer.glue.recognitionqueue.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionWithStatus
import javax.inject.Inject

class EnqueuedToDomainMapper @Inject constructor(
    private val enqueuedStatusToDomainMapper: Mapper<EnqueuedRecognitionDataStatus, EnqueuedRecognitionStatus>
) : Mapper<EnqueuedRecognitionEntityWithStatus, EnqueuedRecognitionWithStatus> {

    override fun map(input: EnqueuedRecognitionEntityWithStatus): EnqueuedRecognitionWithStatus {
        return EnqueuedRecognitionWithStatus(
            id = input.id,
            title = input.title,
            recordFile = input.recordFile,
            creationDate = input.creationDate,
            status = enqueuedStatusToDomainMapper.map(input.status)
        )
    }

}