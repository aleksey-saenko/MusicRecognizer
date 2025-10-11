package com.mrsep.musicrecognizer.core.recognition.audd.websocket

import com.mrsep.musicrecognizer.core.recognition.audd.json.AuddResponseJson
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow

internal interface WebSocketSession {

    fun startReconnectingSession(
        maxReconnectAttempts: Int,
        block: HttpRequestBuilder.() -> Unit,
    ): Flow<Result<WebSocketConnection>>
}

internal interface WebSocketConnection {

    val responseChannel: ReceiveChannel<AuddResponseJson>

    suspend fun sendSample(data: ByteArray)
}
