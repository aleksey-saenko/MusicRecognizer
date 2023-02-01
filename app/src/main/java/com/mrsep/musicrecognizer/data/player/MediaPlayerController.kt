package com.mrsep.musicrecognizer.data.player

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaPlayerController @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private var player: MediaPlayer? = null

    private val fileName = "${appContext.cacheDir.absolutePath}/test_record.m4a"

    fun playAudio() {
        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                setOnPreparedListener { it.start() }
                prepareAsync()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("PLAYER", "prepare() failed")
            }
        }
        Toast.makeText(appContext, "player started", Toast.LENGTH_LONG).show()
    }

     fun stopPlay() {
        player?.apply {
            stop()
            release()
        }
        player = null
        Toast.makeText(appContext, "player stopped", Toast.LENGTH_LONG).show()
    }
}