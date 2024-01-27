package com.mrsep.musicrecognizer.glue.recognition.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionProvider
import javax.inject.Inject

class RecognitionProviderMapper @Inject constructor() :
    Mapper<RecognitionProviderDo, RecognitionProvider> {

    override fun map(input: RecognitionProviderDo): RecognitionProvider {
        return when (input) {
            RecognitionProviderDo.Audd -> RecognitionProvider.Audd
            RecognitionProviderDo.AcrCloud -> RecognitionProvider.AcrCloud
        }
    }

}