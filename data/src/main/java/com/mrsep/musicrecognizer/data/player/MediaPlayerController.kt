package com.mrsep.musicrecognizer.data.player

import android.media.MediaPlayer
import kotlinx.coroutines.flow.*
import java.io.File
import java.lang.IllegalStateException
import javax.inject.Inject

@Suppress("unused")
private const val TAG = "MediaPlayerController"

//TODO: need to handle exceptions
class MediaPlayerController @Inject constructor() : PlayerControllerDo {

    private var player: MediaPlayer? = null

    private val _statusFlow = MutableStateFlow<PlayerStatusDo>(PlayerStatusDo.Idle)
    override val statusFlow = _statusFlow.asStateFlow()

    override fun start(file: File) {
        try {
            stop()
            player = MediaPlayer().apply {
                isLooping = false
                setDataSource(file.absolutePath)
                setOnCompletionListener {
                    _statusFlow.update { PlayerStatusDo.Idle }
                }
                setOnErrorListener { _, what, extra ->
                    _statusFlow.update {
                        PlayerStatusDo.Error(
                            record = file,
                            message = "what=$what, extra=$extra"
                        )
                    }
                    true
                }
                setOnPreparedListener {
                    player?.start()
                    _statusFlow.update { PlayerStatusDo.Started(file) }
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _statusFlow.update {
                PlayerStatusDo.Error(
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
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            _statusFlow.update { PlayerStatusDo.Paused(currentStatus.record) }
        }
    }

    override fun resume() {
        val currentStatus = _statusFlow.value
        if (currentStatus is PlayerStatusDo.Paused) {
            try {
                player?.start()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            _statusFlow.update { PlayerStatusDo.Started(currentStatus.record) }
        }
    }

    override fun stop() {
        player?.apply {
            try {
                stop()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
            release()
        }
        player = null
        _statusFlow.update { PlayerStatusDo.Idle }
    }

}