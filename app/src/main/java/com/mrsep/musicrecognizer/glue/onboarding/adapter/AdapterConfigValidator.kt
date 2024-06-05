package com.mrsep.musicrecognizer.glue.onboarding.adapter

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.ConfigValidationStatusDo
import com.mrsep.musicrecognizer.data.remote.ConfigValidatorDo
import com.mrsep.musicrecognizer.feature.onboarding.domain.ConfigValidator
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.AcrCloudConfig
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.AuddConfig
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.ConfigValidationStatus
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.RecognitionServiceConfig
import javax.inject.Inject

class AdapterConfigValidator @Inject constructor(
    private val configValidator: ConfigValidatorDo,
    private val configValidationStatusMapper: Mapper<ConfigValidationStatusDo, ConfigValidationStatus>,
    private val auddConfigMapper: BidirectionalMapper<AuddConfigDo, AuddConfig>
) : ConfigValidator {

    override suspend fun validate(config: RecognitionServiceConfig): ConfigValidationStatus {
        val configDo = when (config) {
            is AcrCloudConfig -> throw IllegalStateException("Not implemented")
            is AuddConfig -> auddConfigMapper.reverseMap(config)
        }
        return configValidationStatusMapper.map(configValidator.validate(configDo))
    }
}
