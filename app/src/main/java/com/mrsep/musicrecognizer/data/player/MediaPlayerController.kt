package com.mrsep.musicrecognizer.data.player

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import com.mrsep.musicrecognizer.domain.PlayerController
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MediaPlayerController"

@Singleton
class MediaPlayerController @Inject constructor(
    @ApplicationContext private val appContext: Context
): PlayerController {
    private var player: MediaPlayer? = null

    override fun startPlay(file: File) {
        player = MediaPlayer().apply {
            try {
                setDataSource(file.absolutePath)
                setOnPreparedListener { it.start() }
                prepareAsync()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("PLAYER", "prepare() failed")
            }
        }
        Log.d(TAG, "player started")
    }

     override fun stopPlay() {
        player?.apply {
            stop()
            release()
        }
        player = null
         Log.d(TAG, "player stopped")
    }

}