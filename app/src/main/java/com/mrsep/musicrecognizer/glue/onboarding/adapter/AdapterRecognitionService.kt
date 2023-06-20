package com.mrsep.musicrecognizer.glue.onboarding.adapter

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.TokenValidationStatusDo
import com.mrsep.musicrecognizer.data.remote.audd.rest.RecognitionServiceDo
import com.mrsep.musicrecognizer.feature.onboarding.domain.RecognitionService
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.TokenValidationStatus
import javax.inject.Inject

class AdapterRecognitionService @Inject constructor(
    private val recognitionServiceDo: RecognitionServiceDo,
    private val tokenValidationStatusMapper: Mapper<TokenValidationStatusDo, TokenValidationStatus>
): RecognitionService {

    override suspend fun validateToken(token: String): TokenValidationStatus {
        return tokenValidationStatusMapper.map(
            recognitionServiceDo.validateToken(token)
        )
    }
}