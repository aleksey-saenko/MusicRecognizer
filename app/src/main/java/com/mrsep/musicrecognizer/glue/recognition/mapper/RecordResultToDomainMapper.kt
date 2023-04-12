package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.recorder.RecordDataResult
import com.mrsep.musicrecognizer.feature.recognition.domain.RecordResult
import javax.inject.Inject

class RecordResultToDomainMapper @Inject constructor() : Mapper<RecordDataResult, RecordResult> {

    override fun map(input: RecordDataResult): RecordResult {
        return when (input) {
            is RecordDataResult.Error -> RecordResult.Error(input.throwable)
            is RecordDataResult.Success -> RecordResult.Success(input.file)
        }
    }

}