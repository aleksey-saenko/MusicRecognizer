package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.domain.model.Mapper
import javax.inject.Inject

class EnqueuedToDataMapper @Inject constructor() :
    Mapper<EnqueuedRecognition, EnqueuedRecognitionEntity> {

    override fun map(input: EnqueuedRecognition): EnqueuedRecognitionEntity {
        return EnqueuedRecognitionEntity(
            id = input.id,
            title = input.title,
            filepath = input.recordFile.absolutePath,
            creationDate = input.creationDate
        )
    }

}