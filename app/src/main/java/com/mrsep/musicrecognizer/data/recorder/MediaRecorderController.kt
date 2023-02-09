package com.mrsep.musicrecognizer.data.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.mrsep.musicrecognizer.domain.RecorderController
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "MediaRecorderController"

@Singleton
class MediaRecorderController @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) : RecorderController {

    override val recordFile =
        File("${applicationContext.cacheDir.absolutePath}/$RECORD_FILE_NAME")

    private var recorder: MediaRecorder? = null

    @Suppress("DEPRECATION")
    override fun startRecord() {
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.UNPROCESSED)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(recordFile.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOnInfoListener { _, what, extra ->
                Log.d(TAG, "InfoListener: what=$what, extra=$extra")
            }
            setOnErrorListener { _, what, extra ->
                Log.d(TAG, "ErrorListener: what=$what, extra=$extra")
            }
            setMaxDuration(15_000)
//            setPreferredMicrophoneDirection(MicrophoneDirection.MIC_DIRECTION_AWAY_FROM_USER)
//            setPreferredMicrophoneDirection(MicrophoneDirection.MIC_DIRECTION_TOWARDS_USER)
            try {
                prepare()
                start()
                Log.d(TAG, "recorder started")
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e(TAG, "prepare() failed")
            }
        }
    }

    override fun stopRecord() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        Log.d(TAG, "recorder stopped")
    }

    companion object {
        private const val RECORD_FILE_NAME = "mr_record.m4a"
    }

}