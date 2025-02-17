package com.mrsep.musicrecognizer.core.database.enqueued

import androidx.room.*
import com.mrsep.musicrecognizer.core.database.DatabaseUtils.dbChunkedMap
import com.mrsep.musicrecognizer.core.database.DatabaseUtils.eachDbChunk
import com.mrsep.musicrecognizer.core.database.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.core.database.enqueued.model.EnqueuedRecognitionEntityWithTrack
import kotlinx.coroutines.flow.Flow
import java.io.File

@Dao
interface EnqueuedRecognitionDao {

    @Insert
    suspend fun insert(recognition: EnqueuedRecognitionEntity): Long

    @Update
    suspend fun update(recognition: EnqueuedRecognitionEntity)

    @Query("UPDATE enqueued_recognition SET title = :newTitle WHERE id = :recognitionId")
    suspend fun updateTitle(recognitionId: Int, newTitle: String)

    @Query("SELECT record_file FROM enqueued_recognition WHERE id = :recognitionId")
    suspend fun getRecordingFile(recognitionId: Int): File?

    @Transaction
    suspend fun getRecordingFiles(recognitionIds: List<Int>): List<File> {
        return recognitionIds.dbChunkedMap(::getRecordingFilesInternal)
    }

    @Query("SELECT record_file FROM enqueued_recognition WHERE id IN (:recognitionIds)")
    suspend fun getRecordingFilesInternal(recognitionIds: List<Int>): List<File>

    @Transaction
    suspend fun delete(recognitionIds: List<Int>) {
        recognitionIds.eachDbChunk(::deleteInternal)
    }

    @Query("DELETE FROM enqueued_recognition WHERE id IN (:recognitionIds)")
    suspend fun deleteInternal(recognitionIds: List<Int>)

    @Query("DELETE FROM enqueued_recognition")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM enqueued_recognition WHERE id = :recognitionId LIMIT 1")
    fun getRecognitionWithTrackFlow(recognitionId: Int): Flow<EnqueuedRecognitionEntityWithTrack?>

    @Transaction
    @Query("SELECT * FROM enqueued_recognition ORDER BY creation_date DESC")
    fun getAllRecognitionsWithTrackFlow(): Flow<List<EnqueuedRecognitionEntityWithTrack>>
}
