package com.mrsep.musicrecognizer.util

import android.content.Context
import android.widget.Toast
import com.mrsep.musicrecognizer.di.IoDispatcher
import com.mrsep.musicrecognizer.domain.TrackRepository
import com.mrsep.musicrecognizer.domain.model.RemoteRecognitionResult
import com.mrsep.musicrecognizer.domain.model.Track
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.IOException
import javax.inject.Inject

private const val TAG = "DatabaseFiller"

class DatabaseFiller @Inject constructor(
    @ApplicationContext private val appContext: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val trackRepository: TrackRepository,
    private val moshi: Moshi
) {
    private val defaultAssetsDirectory = "success_json_responses"

    suspend fun prepopulateDatabaseFromAssets(assetsDirectory: String = defaultAssetsDirectory) {
        withContext(ioDispatcher) {
            val trackList = parseJsonFilesFromAssets(assetsDirectory)
                .filterIsInstance<RemoteRecognitionResult.Success<Track>>()
                .mapIndexed { index, result ->
                    result.data.run {
                        if (index < 4) { // make first 5 tracks favorites
                            copy(metadata = result.data.metadata.copy(isFavorite = true))
                        } else {
                            this
                        }
                    }
                }.toTypedArray()
            trackRepository.insertOrReplace(*trackList)
        }
    }

    private suspend fun parseJsonFilesFromAssets(assetsDirectory: String): List<RemoteRecognitionResult<Track>> {
        return withContext(ioDispatcher) {
            val resultType = Types.newParameterizedType(
                RemoteRecognitionResult::class.java,
                Track::class.java
            )
            val jsonAdapter = moshi.adapter<RemoteRecognitionResult<Track>>(resultType)

            val fileNamesArray = try {
                appContext.assets.list(assetsDirectory) ?: emptyArray()
            } catch (e: IOException) {
                e.printStackTrace()
                e.showNameInToast()
                return@withContext emptyList()
            }
            fileNamesArray.mapNotNull { fileName ->
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