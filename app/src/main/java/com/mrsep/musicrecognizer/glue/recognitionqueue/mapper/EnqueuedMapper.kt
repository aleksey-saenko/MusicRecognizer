package com.mrsep.musicrecognizer.glue.recognitionqueue.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionDo
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionStatusDo
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognitionStatus
import com.mrsep.musicrecognizer.feature.recognitionqueue.domain.model.EnqueuedRecognition
import javax.inject.Inject

class EnqueuedMapper @Inject constructor(
    private val enqueuedStatusMapper: Mapper<EnqueuedRecognitionStatusDo, EnqueuedRecognitionStatus>
) : Mapper<EnqueuedRecognitionDo, EnqueuedRecognition> {

    override fun map(input: EnqueuedRecognitionDo): EnqueuedRecognition {
        return EnqueuedRecognition(
            id = input.id,
            title = input.title,
            recordFile = input.recordFile,
            creationDate = input.creationDate,
            status = enqueuedStatusMapper.map(input.status)
        )
    }

}