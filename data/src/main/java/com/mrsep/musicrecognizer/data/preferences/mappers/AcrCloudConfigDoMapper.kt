package com.mrsep.musicrecognizer.data.preferences.mappers

import com.mrsep.musicrecognizer.AcrCloudConfigProto
import com.mrsep.musicrecognizer.acrCloudConfigProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import javax.inject.Inject

class AcrCloudConfigDoMapper @Inject constructor() :
    BidirectionalMapper<AcrCloudConfigProto, AcrCloudConfigDo> {

    override fun map(input: AcrCloudConfigProto): AcrCloudConfigDo {
        return AcrCloudConfigDo(
            host = input.host,
            accessKey = input.accessKey,
            accessSecret = input.accessSecret,
        )
    }

    override fun reverseMap(input: AcrCloudConfigDo): AcrCloudConfigProto {
        return acrCloudConfigProto {
            host = input.host
            accessKey = input.accessKey
            accessSecret = input.accessSecret
        }
    }
}
