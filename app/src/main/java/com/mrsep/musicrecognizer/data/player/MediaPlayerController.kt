package com.mrsep.musicrecognizer.data.player

import android.media.MediaPlayer
import com.mrsep.musicrecognizer.domain.PlayerController
import com.mrsep.musicrecognizer.domain.PlayerStatus
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MediaPlayerController"

@Singleton
class MediaPlayerController @Inject constructor() : PlayerController {

    private var player: MediaPlayer? = null

    private val _statusFlow = MutableStateFlow<PlayerStatus>(PlayerStatus.Idle)
    override val statusFlow = _statusFlow.asStateFlow()

    override fun start(file: File) {
        stop()
        player = MediaPlayer().apply {
            isLooping = false
            setDataSource(file.absolutePath)
            setOnCompletionListener {
                _statusFlow.update { PlayerStatus.Idle }
            }
            setOnErrorListener { _, what, extra ->
                _statusFlow.update { PlayerStatus.Error(file, "what=$what, extra=$extra") }
                true
            }
            setOnPreparedListener {
                player?.start()
                _statusFlow.update { PlayerStatus.Started(file) }
            }
            prepareAsync()
        }
    }

    override fun pause() {
        val currentStatus = _statusFlow.value
        if (currentStatus is PlayerStatus.Started) {
            player?.pause()
            _statusFlow.update { PlayerStatus.Paused(currentStatus.record) }
        }
    }

    override fun resume() {
        val currentStatus = _statusFlow.value
        if (currentStatus is PlayerStatus.Paused) {
            player?.start()
            _statusFlow.update { PlayerStatus.Started(currentStatus.record) }
        }
    }

    override fun stop() {
        player?.apply {
            stop()
            release()
        }
        player = null
        _statusFlow.update { PlayerStatus.Idle }
    }

}

//data class PlayerPosition(
//    val currentPositionMs: Int,
//    val totalDurationMs: Int
//)
//
//@OptIn(ExperimentalCoroutinesApi::class)
//val positionFlow = _statusFlow.transformLatest { status ->
//    when (status) {
//        is PlayerStatus.Error ->
//        PlayerStatus.Idle ->
//        is PlayerStatus.Paused ->
//        is PlayerStatus.Started -> {
//            while (true) {
//                player?.let { mp ->
//                    emit(PlayerPosition(mp.currentPosition, mp.duration))
//                }
//                delay(500)
//            }
//        }
//    }
//}