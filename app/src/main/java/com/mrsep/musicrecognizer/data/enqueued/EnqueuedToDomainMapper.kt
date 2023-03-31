package com.mrsep.musicrecognizer.data.enqueued

import com.mrsep.musicrecognizer.domain.model.EnqueuedRecognition
import com.mrsep.musicrecognizer.domain.model.Mapper
import java.io.File
import javax.inject.Inject

class EnqueuedToDomainMapper @Inject constructor() :
    Mapper<EnqueuedRecognitionEntity, EnqueuedRecognition> {

    override fun map(input: EnqueuedRecognitionEntity): EnqueuedRecognition {
        return EnqueuedRecognition(
            id = input.id,
            title = input.title,
            recordFile = File(input.filepath),
            creationDate = input.creationDate
        )
    }

}