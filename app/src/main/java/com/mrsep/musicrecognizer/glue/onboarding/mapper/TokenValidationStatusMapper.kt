package com.mrsep.musicrecognizer.glue.onboarding.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.TokenValidationStatusDo
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.TokenValidationStatus
import javax.inject.Inject

class TokenValidationStatusMapper @Inject constructor():
    Mapper<TokenValidationStatusDo, TokenValidationStatus> {

    override fun map(input: TokenValidationStatusDo): TokenValidationStatus {
        return when (input) {
            is TokenValidationStatusDo.Error -> when (val error = input.e) {
                RemoteRecognitionResultDo.Error.BadConnection ->
                    TokenValidationStatus.Error.BadConnection
                is RemoteRecognitionResultDo.Error.WrongToken ->
                    TokenValidationStatus.Error.WrongToken(error.isLimitReached)
                else -> TokenValidationStatus.Error.UnknownError
            }
            TokenValidationStatusDo.Success -> TokenValidationStatus.Success
        }
    }
}