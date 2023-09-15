package com.mrsep.musicrecognizer.data.remote.audd.websocket

import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.remote.audd.toAuddReturnParameter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.lang.Exception
import javax.inject.Inject

@Suppress("unused")
private const val TAG = "AuddRecognitionWebSocketServiceImpl"

private const val AUDD_WEB_SOCKET_URL = "wss://api.audd.io/ws/?return=%s&api_token=%s"

class AuddRecognitionWebSocketServiceImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val moshi: Moshi
) : AuddRecognitionWebSocketService {

    private fun buildRequest(
        token: String,
        requiredServices: UserPreferencesDo.RequiredServicesDo
    ): Request {
        val returnParam = requiredServices.toAuddReturnParameter()
        return Request.Builder()
            .url(AUDD_WEB_SOCKET_URL.format(returnParam, token))
            .build()
    }

    override suspend fun startSession(
        token: String,
        requiredServices: UserPreferencesDo.RequiredServicesDo
    ): Flow<SocketEvent> = flow {
        while (true) {
            emitAll(startSingleSession(token, requiredServices))
        }
    }

    private suspend fun startSingleSession(
        token: String,
        requiredServices: UserPreferencesDo.RequiredServicesDo
    ): Flow<SocketEvent> = callbackFlow {
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
        val request = buildRequest(token, requiredServices)
        val webSocket = okHttpClient.newWebSocket(
            request,
            eventsListener
        )
        awaitClose {
            webSocket.cancel()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun parseServerResponse(json: String): RemoteRecognitionResultDo {
        return try {
            moshi.adapter<RemoteRecognitionResultDo>().fromJson(json)!!
        } catch (e: Exception) {
            RemoteRecognitionResultDo.Error.UnhandledError(message = e.message ?: "", e = e)
        }
    }

}