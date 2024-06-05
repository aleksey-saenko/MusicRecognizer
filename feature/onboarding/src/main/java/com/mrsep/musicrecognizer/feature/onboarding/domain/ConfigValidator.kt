package com.mrsep.musicrecognizer.feature.onboarding.domain

import com.mrsep.musicrecognizer.feature.onboarding.domain.model.ConfigValidationStatus
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.RecognitionServiceConfig

interface ConfigValidator {

    suspend fun validate(config: RecognitionServiceConfig): ConfigValidationStatus
}
