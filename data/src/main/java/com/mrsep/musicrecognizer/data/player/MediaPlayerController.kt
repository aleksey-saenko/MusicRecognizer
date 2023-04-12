package com.mrsep.musicrecognizer.data.player

import android.media.MediaPlayer
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject

private const val TAG = "MediaPlayerController"

class MediaPlayerController @Inject constructor() : PlayerDataController {

    private var player: MediaPlayer? = null

    private val _statusFlow = MutableStateFlow<PlayerDataStatus>(PlayerDataStatus.Idle)
    override val statusFlow = _statusFlow.asStateFlow()

    override fun start(file: File) {
        stop()
        player = MediaPlayer().apply {
            isLooping = false
            setDataSource(file.absolutePath)
            setOnCompletionListener {
                _statusFlow.update { PlayerDataStatus.Idle }
            }
            setOnErrorListener { _, what, extra ->
                _statusFlow.update { PlayerDataStatus.Error(file, "what=$what, extra=$extra") }
                true
            }
            setOnPreparedListener {
                player?.start()
                _statusFlow.update { PlayerDataStatus.Started(file) }
            }
            prepareAsync()
        }
    }

    override fun pause() {
        val currentStatus = _statusFlow.value
        if (currentStatus is PlayerDataStatus.Started) {
            player?.pause()
            _statusFlow.update { PlayerDataStatus.Paused(currentStatus.record) }
        }
    }

    override fun resume() {
        val currentStatus = _statusFlow.value
        if (currentStatus is PlayerDataStatus.Paused) {
            player?.start()
            _statusFlow.update { PlayerDataStatus.Started(currentStatus.record) }
        }
    }

    override fun stop() {
        player?.apply {
            stop()
            release()
        }
        player = null
        _statusFlow.update { PlayerDataStatus.Idle }
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