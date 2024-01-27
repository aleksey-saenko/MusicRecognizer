package com.mrsep.musicrecognizer.glue.preferences.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.feature.preferences.domain.AcrCloudConfig
import javax.inject.Inject

class AcrCloudConfigMapper @Inject constructor() :
    BidirectionalMapper<AcrCloudConfigDo, AcrCloudConfig> {

    override fun map(input: AcrCloudConfigDo): AcrCloudConfig {
        return AcrCloudConfig(
            host = input.host,
            accessKey = input.accessKey,
            accessSecret = input.accessSecret
        )
    }

    override fun reverseMap(input: AcrCloudConfig): AcrCloudConfigDo {
        return AcrCloudConfigDo(
            host = input.host,
            accessKey = input.accessKey,
            accessSecret = input.accessSecret
        )
    }

}