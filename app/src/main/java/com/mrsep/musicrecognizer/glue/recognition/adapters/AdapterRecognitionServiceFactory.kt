package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionServiceFactoryDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.feature.recognition.domain.RecognitionServiceFactory
import com.mrsep.musicrecognizer.feature.recognition.domain.RemoteRecognitionService
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AcrCloudConfig
import com.mrsep.musicrecognizer.feature.recognition.domain.model.AuddConfig
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionServiceConfig
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RemoteRecognitionResult
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class AdapterRecognitionServiceFactory @Inject constructor(
    private val factoryDo: RecognitionServiceFactoryDo,
    private val remoteResultMapper: Mapper<RemoteRecognitionResultDo, RemoteRecognitionResult>,
    private val auddConfigMapper: BidirectionalMapper<AuddConfigDo, AuddConfig>,
    private val acrCloudConfigMapper: BidirectionalMapper<AcrCloudConfigDo, AcrCloudConfig>,
) : RecognitionServiceFactory {

    override fun getService(config: RecognitionServiceConfig): RemoteRecognitionService {
        val configDo = when (config) {
            is AuddConfig -> auddConfigMapper.reverseMap(config)
            is AcrCloudConfig -> acrCloudConfigMapper.reverseMap(config)
        }
        val serviceDo = factoryDo.getService(configDo)

        return object : RemoteRecognitionService {

            override suspend fun recognize(
                audioRecordingFlow: Flow<ByteArray>
            ): RemoteRecognitionResult {
                return remoteResultMapper.map(
                    serviceDo.recognize(recordingFlow = audioRecordingFlow)
                )
            }

            override suspend fun recognize(file: File): RemoteRecognitionResult {
                return remoteResultMapper.map(
                    serviceDo.recognize(recording = file)
                )
            }

        }
    }

}