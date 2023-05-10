package com.mrsep.musicrecognizer.feature.recognition.presentation.service

import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.selects.select
import javax.inject.Inject

class RecognitionStateDelegator @Inject constructor(
//    private val interactor: RecognitionInteractor
): ResultReceiver, MainResultProducer, ServiceResultProducer {

    private val mainChannel = Channel<RecognitionStatus>(Channel.RENDEZVOUS)
    private val serviceChannel = Channel<RecognitionStatus>(Channel.RENDEZVOUS)

    override val mainResultFlow: ReceiveChannel<RecognitionStatus>
        get() = mainChannel

    override val serviceResultFlow: ReceiveChannel<RecognitionStatus>
        get() = serviceChannel

    override suspend fun receiveResult(result: RecognitionStatus) {
        select<Unit> {
            mainChannel.onSend(result) {  }
            serviceChannel.onSend(result) {  }
        }
    }


}

interface ResultReceiver {
    suspend fun receiveResult(result: RecognitionStatus)
}

interface MainResultProducer {
    val mainResultFlow: ReceiveChannel<RecognitionStatus>
}

interface ServiceResultProducer {
    val serviceResultFlow: ReceiveChannel<RecognitionStatus>
}