package com.mrsep.musicrecognizer.data.track.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.remote.RemoteRecognitionResultDo
import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import java.io.IOException
import javax.inject.Inject

private const val TAG = "DatabaseFiller"

class DatabaseFiller @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val trackRepository: TrackRepositoryDo,
    private val moshi: Moshi
) {
    private val defaultAssetsDirectory = "success_json_responses"

    suspend fun prepopulateByFaker(force: Boolean = true, count: Int) {
        withContext(ioDispatcher) {
            if (force || trackRepository.isEmptyFlow().first()) {
                getFakeTrackList(
                    startIndex = 0,
                    endIndex = count,
                    inclusive = false,
                    favorites = false
                ).toTypedArray().run { trackRepository.insertOrReplace(*this) }
                Log.d(TAG, "Database filling completed")
            }
        }
    }

    suspend fun prepopulateFromAssets(
        force: Boolean = true,
        assetsDirectory: String = defaultAssetsDirectory
    ) {
        withContext(ioDispatcher) {
            if (force || trackRepository.isEmptyFlow().first()) {
                val trackList = parseJsonFilesFromAssets(assetsDirectory)
                    .filterIsInstance<RemoteRecognitionResultDo.Success>()
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
    }

    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun parseJsonFilesFromAssets(assetsDirectory: String): List<RemoteRecognitionResultDo> {
        return withContext(ioDispatcher) {
            val jsonAdapter = moshi.adapter<RemoteRecognitionResultDo>()

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