package com.mrsep.musicrecognizer.data.audioplayer

import android.media.MediaPlayer
import android.util.Log
import com.mrsep.musicrecognizer.core.common.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalStateException
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@Suppress("unused")
private const val TAG = "MediaPlayerController"

class MediaPlayerController @Inject constructor(
    @DefaultDispatcher private val pollingDispatcher: CoroutineDispatcher
) : PlayerControllerDo {

    private var player: MediaPlayer? = null

    private val playerCoroutineScope = CoroutineScope(pollingDispatcher + SupervisorJob())
    private var positionPollingJob: Job? = null

    private val _currentPosition = Channel<Int>(Channel.CONFLATED)
    override val playbackPositionFlow = _currentPosition.receiveAsFlow()

    private val _statusFlow = MutableStateFlow<PlayerStatusDo>(PlayerStatusDo.Idle)
    override val statusFlow = _statusFlow.asStateFlow()

    override fun start(id: Int, file: File) {
        try {
            stopWithStatus(PlayerStatusDo.Idle)
            player = MediaPlayer().apply {
                isLooping = false
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    stopWithStatus(PlayerStatusDo.Idle)
                }
                setOnErrorListener { _, what, extra ->
                    val errorStatus = PlayerStatusDo.Error(
                        id = id,
                        record = file,
                        message = "what=$what, extra=$extra"
                    )
                    stopWithStatus(errorStatus)
                    true
                }
                setOnPreparedListener { mediaPlayer ->
                    mediaPlayer.start()
                    _statusFlow.update {
                        PlayerStatusDo.Started(
                            id = id,
                            record = file,
                            duration = mediaPlayer.duration.milliseconds
                        )
                    }
                    launchPositionPolling()
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            _statusFlow.update {
                PlayerStatusDo.Error(
                    id = id,
                    record = file,
                    message = e::class.java.simpleName
                )
            }
        }
    }

    override fun pause() {
        val currentStatus = _statusFlow.value
        if (currentStatus is PlayerStatusDo.Started) {
            try {
                player?.pause()
                positionPollingJob?.cancel()
                _statusFlow.update {
                    PlayerStatusDo.Paused(
                        id = currentStatus.id,
                        record = currentStatus.record,
                        duration = (player?.duration ?: -1).milliseconds
                    )
                }
            } catch (e: IllegalStateException) {
                Log.e(this::class.simpleName, "Pause command at illegal time", e)
            }
        }
    }

    override fun resume() {
        val currentStatus = _statusFlow.value
        if (currentStatus is PlayerStatusDo.Paused) {
            try {
                player?.start()
                launchPositionPolling()
                _statusFlow.update {
                    PlayerStatusDo.Started(
                        id = currentStatus.id,
                        record = currentStatus.record,
                        duration = (player?.duration ?: -1).milliseconds
                    )
                }
            } catch (e: IllegalStateException) {
                Log.e(this::class.simpleName, "Resume command at illegal time", e)
            }
        }
    }

    override fun stop() {
        stopWithStatus(PlayerStatusDo.Idle)
    }

    private fun stopWithStatus(playerStatus: PlayerStatusDo) {
        stopPollingAndResetPosition()
        try {
            player?.stop()
        } catch (e: IllegalStateException) {
            Log.e(this::class.simpleName, "Stop command at illegal time", e)
        }
        player?.release()
        player = null
        _statusFlow.update { playerStatus }
    }

    private fun launchPositionPolling() {
        positionPollingJob = playerCoroutineScope.launch {
            runCatching {
                while (player?.isPlaying == true) {
                    player?.currentPosition?.run { _currentPosition.send(this) }
                    delay(POSITION_POLLING_RATE_MS)
                }
            }
        }
    }

    private fun stopPollingAndResetPosition() {
        playerCoroutineScope.launch {
            positionPollingJob?.cancelAndJoin()
            _currentPosition.send(0)
        }
    }

    companion object {
        private const val POSITION_POLLING_RATE_MS = 100L
    }
}
