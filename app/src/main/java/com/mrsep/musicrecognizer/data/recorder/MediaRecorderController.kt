package com.mrsep.musicrecognizer.data.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRecorderController @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {
    private var recorder: MediaRecorder? = null

    @Suppress("DEPRECATION")
    fun startRecord() {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.UNPROCESSED)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(getRecordFile(applicationContext).absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOnInfoListener { recorder, what, extra ->
                Log.d(
                    recorder::class.simpleName,
                    "InfoListener: what=$what, extra=$extra"
                )
            }
            setOnErrorListener { recorder, what, extra ->
                Log.d(
                    recorder::class.simpleName,
                    "ErrorListener: what=$what, extra=$extra"
                )
            }
            setMaxDuration(15_000)
//            setPreferredMicrophoneDirection(MicrophoneDirection.MIC_DIRECTION_AWAY_FROM_USER)
//            setPreferredMicrophoneDirection(MicrophoneDirection.MIC_DIRECTION_TOWARDS_USER)
            try {
                prepare()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("RECORDER", "prepare() failed")
            }

            start()
        }

        Toast.makeText(applicationContext, "recorder started", Toast.LENGTH_LONG).show()
    }

    fun stopRecord() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        Toast.makeText(applicationContext, "recorder stopped", Toast.LENGTH_LONG).show()
    }

    companion object {
        private const val recordFileName = "mr_record.m4a"
        fun getRecordFile(context: Context) = File("${context.cacheDir.absolutePath}/$recordFileName")
    }

}