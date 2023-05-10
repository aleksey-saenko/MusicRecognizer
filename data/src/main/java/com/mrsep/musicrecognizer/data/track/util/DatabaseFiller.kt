package com.mrsep.musicrecognizer.data.track.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionDataResult
import com.mrsep.musicrecognizer.data.track.TrackDataRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.IOException
import javax.inject.Inject

private const val TAG = "DatabaseFiller"

class DatabaseFiller @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val trackRepository: TrackDataRepository,
    private val moshi: Moshi
) {
    private val defaultAssetsDirectory = "success_json_responses"

    suspend fun prepopulateByFaker(count: Int) {
        withContext(ioDispatcher) {
            getFakeTrackList(
                startIndex = 0,
                endIndex = count,
                inclusive = false,
                favorites = false
            ).toTypedArray().run { trackRepository.insertOrReplace(*this) }
            Log.d(TAG, "Database filling completed")
        }
    }

    suspend fun prepopulateFromAssets(assetsDirectory: String = defaultAssetsDirectory) {
        withContext(ioDispatcher) {
            val trackList = parseJsonFilesFromAssets(assetsDirectory)
                .filterIsInstance<RemoteRecognitionDataResult.Success>()
                .mapIndexed { index, result ->
                    result.data.run {
                        if (index < 9) { // make first 9 tracks favorites
                            copy(metadata = result.data.metadata.copy(isFavorite = true))
                        } else {
                            this
                        }
                    }
                }.toTypedArray()
            trackRepository.insertOrReplace(*trackList)
            Log.d(TAG, "Database filling completed")
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun parseJsonFilesFromAssets(assetsDirectory: String): List<RemoteRecognitionDataResult> {
        return withContext(ioDispatcher) {
            val jsonAdapter = moshi.adapter<RemoteRecognitionDataResult>()

            val fileNamesArray = try {
                appContext.assets.list(assetsDirectory) ?: emptyArray()
            } catch (e: IOException) {
                e.printStackTrace()
                e.showNameInToast()
                return@withContext emptyList()
            }
            fileNamesArray.mapNotNull { fileName ->
                delay(1) //for individual timestamps
                try {
                    jsonAdapter.fromJson(
                        appContext.assets.open("${assetsDirectory}/${fileName}")
                            .bufferedReader().use { it.readText() }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    e.showNameInToast()
                    null
                }
            }
        }
    }


    private fun Exception.showNameInToast() {
        Toast.makeText(appContext, this::class.simpleName, Toast.LENGTH_LONG).show()
    }

}