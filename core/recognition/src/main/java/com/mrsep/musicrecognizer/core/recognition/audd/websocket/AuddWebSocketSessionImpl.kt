package com.mrsep.musicrecognizer.core.recognition.audd.websocket

import com.mrsep.musicrecognizer.core.domain.recognition.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.core.recognition.audd.json.AuddResponseJson
import com.mrsep.musicrecognizer.core.recognition.audd.json.toRecognitionResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.lang.Exception
import javax.inject.Inject

internal class AuddWebSocketSessionImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val json: Json,
) : AuddWebSocketSession {

    private fun buildRequest(token: String): Request {
        return Request.Builder()
            .url(WEB_SOCKET_URL.format(RETURN_PARAM, token))
            .build()
    }

    override suspend fun startSession(apiToken: String): Flow<SocketEvent> = flow {
        var reconnectionDelay = 1000L
        while (true) {
            emitAll(startSingleSession(apiToken))
            delay(reconnectionDelay)
            if (reconnectionDelay < 4000L) reconnectionDelay *= 2
        }
    }

    private suspend fun startSingleSession(token: String): Flow<SocketEvent> = callbackFlow {
        val eventsListener = object : WebSocketListener() {
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                trySendBlocking(SocketEvent.ConnectionClosed(ShutdownReason(code, reason)))
                close()
            }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                trySendBlocking(SocketEvent.ConnectionClosing(ShutdownReason(code, reason)))
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                trySendBlocking(SocketEvent.ConnectionFailed(t))
                close()
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                trySendBlocking(SocketEvent.ResponseReceived(parseServerResponse(text)))
            }
            override fun onOpen(webSocket: WebSocket, response: Response) {
                trySendBlocking(SocketEvent.ConnectionOpened(webSocket))
            }
        }
        val request = buildRequest(token)
        val webSocket = okHttpClient.newWebSocket(
            request,
            eventsListener
        )
        awaitClose {
            webSocket.cancel()
        }
    }

    private fun parseServerResponse(responseJson: String): RemoteRecognitionResult {
        return try {
            json.decodeFromString<AuddResponseJson>(responseJson).toRecognitionResult(json)
        } catch (e: Exception) {
            RemoteRecognitionResult.Error.UnhandledError(message = e.message ?: "", cause = e)
        }
    }

    companion object {
        private const val WEB_SOCKET_URL = "wss://api.audd.io/ws/?return=%s&api_token=%s"
        private const val RETURN_PARAM = "lyrics,spotify,apple_music,deezer,napster,musicbrainz"
    }
}
