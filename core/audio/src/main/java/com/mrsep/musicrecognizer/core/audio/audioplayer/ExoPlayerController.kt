package com.mrsep.musicrecognizer.core.audio.audioplayer

import android.content.Context
import android.os.Handler
import androidx.annotation.MainThread
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.audio.MediaCodecAudioRenderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.metadata.MetadataOutput
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.text.TextOutput
import androidx.media3.exoplayer.video.VideoRendererEventListener
import androidx.media3.extractor.Extractor
import androidx.media3.extractor.ExtractorsFactory
import androidx.media3.extractor.mp4.Mp4Extractor
import androidx.media3.extractor.text.DefaultSubtitleParserFactory
import com.mrsep.musicrecognizer.core.common.di.MainDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@Suppress("unused")
private const val TAG = "ExoPlayerController"

@UnstableApi
internal class ExoPlayerController @Inject constructor(
    @MainDispatcher private val pollingDispatcher: CoroutineDispatcher,
    @ApplicationContext private val context: Context,
) : PlayerController {

    private val _statusFlow = MutableStateFlow<PlayerStatus>(PlayerStatus.Idle)
    override val statusFlow = _statusFlow.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    override val playbackPositionMs = _playbackPositionMs.asStateFlow()

    private val playerCoroutineScope = CoroutineScope(pollingDispatcher + SupervisorJob())
    private var positionPollingJob: Job? = null

    private var player: ExoPlayer? = null

    @MainThread
    override fun start(id: Int, file: File) {
        stop()
        player = ExoPlayer.Builder(
            context,
            audioOnlyRenderersFactory,
            DefaultMediaSourceFactory(context, mp4ExtractorFactory)
        ).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                /* handleAudioFocus = */ true
            )
            setHandleAudioBecomingNoisy(true)

            addListener(object : Player.Listener {

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) stop()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        _statusFlow.update {
                            PlayerStatus.Started(
                                id = id,
                                record = file,
                                duration = duration.milliseconds
                            )
                        }
                        startPositionPolling()
                    } else {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                _statusFlow.update {
                                    PlayerStatus.Paused(
                                        id = id,
                                        record = file,
                                        duration = duration.milliseconds
                                    )
                                }
                                stopPositionPolling()
                            }

                            Player.STATE_IDLE,
                            Player.STATE_ENDED -> {
                                _statusFlow.update { PlayerStatus.Idle }
                                stopPositionPollingAndResetPosition()
                            }

                            Player.STATE_BUFFERING -> {}
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _statusFlow.update {
                        PlayerStatus.Error(
                            id = id,
                            record = file,
                            message = "${error.errorCodeName} (${error.message})"
                        )
                    }
                }
            })
            setMediaItem(MediaItem.fromUri(file.toUri()))
            prepare()
            play()
        }
    }

    @MainThread
    override fun pause() {
        if (_statusFlow.value is PlayerStatus.Started) {
            player?.pause()
        }
    }

    @MainThread
    override fun resume() {
        if (_statusFlow.value is PlayerStatus.Paused) {
            player?.play()
        }
    }

    @MainThread
    override fun stop() {
        stopPositionPollingAndResetPosition()
        player?.stop()
        player?.release()
        player = null
        _statusFlow.update { PlayerStatus.Idle }
    }

    private fun startPositionPolling() {
        if (positionPollingJob?.isActive == true) return
        positionPollingJob = playerCoroutineScope.launch {
            while (player?.isPlaying == true) {
                player?.currentPosition?.let { pos ->
                    _playbackPositionMs.update { pos }
                }
                delay(POSITION_POLLING_RATE_MS)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = null
    }

    private fun stopPositionPollingAndResetPosition() {
        stopPositionPolling()
        _playbackPositionMs.update { 0L }
    }

    // We don't need to play video, so remove video renders by code shrinking
    // see https://developer.android.com/media/media3/exoplayer/shrinking
    private val audioOnlyRenderersFactory = RenderersFactory {
            handler: Handler,
            _: VideoRendererEventListener,
            audioListener: AudioRendererEventListener,
            _: TextOutput,
            _: MetadataOutput,
        ->
        arrayOf<Renderer>(
            MediaCodecAudioRenderer(context, MediaCodecSelector.DEFAULT, handler, audioListener)
        )
    }

    // We need Mp4Extractor only, so remove other extractors by code shrinking
    private val mp4ExtractorFactory = ExtractorsFactory {
        arrayOf<Extractor>(Mp4Extractor(DefaultSubtitleParserFactory()))
    }

    companion object {
        private const val POSITION_POLLING_RATE_MS = 100L
    }
}
