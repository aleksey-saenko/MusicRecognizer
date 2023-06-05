package com.mrsep.musicrecognizer.glue.recognitionqueue.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionData
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDataStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognition
import javax.inject.Inject

class EnqueuedMapper @Inject constructor(
    private val enqueuedStatusToDomainMapper: Mapper<EnqueuedRecognitionDataStatus, EnqueuedRecognitionStatus>
) : Mapper<EnqueuedRecognitionData, EnqueuedRecognition> {

    override fun map(input: EnqueuedRecognitionData): EnqueuedRecognition {
        return EnqueuedRecognition(
            id = input.id,
            title = input.title,
            recordFile = input.recordFile,
            creationDate = input.creationDate,
            status = enqueuedStatusToDomainMapper.map(input.status)
        )
    }

}