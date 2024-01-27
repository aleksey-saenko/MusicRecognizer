package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.feature.preferences.domain.RecognitionProvider
import javax.inject.Inject

class RecognitionProviderMapper @Inject constructor() :
    BidirectionalMapper<RecognitionProviderDo, RecognitionProvider> {

    override fun map(input: RecognitionProviderDo): RecognitionProvider {
        return when (input) {
            RecognitionProviderDo.Audd -> RecognitionProvider.Audd
            RecognitionProviderDo.AcrCloud -> RecognitionProvider.AcrCloud
        }
    }

    override fun reverseMap(input: RecognitionProvider): RecognitionProviderDo {
        return when (input) {
            RecognitionProvider.Audd -> RecognitionProviderDo.Audd
            RecognitionProvider.AcrCloud -> RecognitionProviderDo.AcrCloud
        }
    }

}