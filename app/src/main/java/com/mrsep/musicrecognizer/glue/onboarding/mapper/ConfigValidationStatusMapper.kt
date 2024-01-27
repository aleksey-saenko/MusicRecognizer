package com.mrsep.musicrecognizer.glue.onboarding.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.ConfigValidationStatusDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.ConfigValidationStatus
import javax.inject.Inject

class ConfigValidationStatusMapper @Inject constructor():
    Mapper<ConfigValidationStatusDo, ConfigValidationStatus> {

    override fun map(input: ConfigValidationStatusDo): ConfigValidationStatus {
        return when (input) {
            is ConfigValidationStatusDo.Error -> when (val error = input.e) {
                RemoteRecognitionResultDo.Error.BadConnection ->
                    ConfigValidationStatus.Error.BadConnection
                is RemoteRecognitionResultDo.Error.WrongToken ->
                    ConfigValidationStatus.Error.WrongToken(error.isLimitReached)
                else -> ConfigValidationStatus.Error.UnknownError
            }
            ConfigValidationStatusDo.Success -> ConfigValidationStatus.Success
        }
    }

}