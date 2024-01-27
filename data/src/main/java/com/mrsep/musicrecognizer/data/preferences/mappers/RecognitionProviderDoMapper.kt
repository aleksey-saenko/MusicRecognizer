package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.RecognitionProviderProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import javax.inject.Inject

class RecognitionProviderDoMapper @Inject constructor():
    BidirectionalMapper<RecognitionProviderProto, RecognitionProviderDo> {

    override fun reverseMap(input: RecognitionProviderDo): RecognitionProviderProto {
        return when (input) {
            RecognitionProviderDo.Audd -> RecognitionProviderProto.Audd
            RecognitionProviderDo.AcrCloud -> RecognitionProviderProto.AcrCloud
        }
    }

    override fun map(input: RecognitionProviderProto): RecognitionProviderDo {
        return when (input) {
            RecognitionProviderProto.UNRECOGNIZED -> RecognitionProviderDo.Audd
            RecognitionProviderProto.Audd -> RecognitionProviderDo.Audd
            RecognitionProviderProto.AcrCloud -> RecognitionProviderDo.AcrCloud
        }
    }

}