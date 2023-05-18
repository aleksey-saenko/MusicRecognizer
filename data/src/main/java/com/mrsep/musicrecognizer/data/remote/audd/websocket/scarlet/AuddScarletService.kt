package com.mrsep.musicrecognizer.data.remote.audd.websocket.scarlet

import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface AuddScarletApi {
    @Receive
    fun observeEvents(): Flow<WebSocket.Event>

    @Send
    fun sendRecording(recordingBody: String): Boolean

    @Receive
    fun observeResponse(): Flow<RemoteRecognitionDataResult>
}

class AuddScarletService @Inject constructor(
    private val auddScarletApi: AuddScarletApi
) {

}


//https://github.com/Tinder/Scarlet/issues/114
//https://vedraj360.medium.com/implement-socket-using-scarlet-android-2023-863922a4bfa8
//https://medium.com/tinder/taming-websocket-with-scarlet-f01125427677