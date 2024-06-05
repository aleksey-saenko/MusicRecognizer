package com.mrsep.musicrecognizer.data.enqueued

import androidx.room.*
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithTrack
import kotlinx.coroutines.flow.Flow
import java.io.File

@Dao
internal interface EnqueuedRecognitionDao {

    @Insert
    suspend fun insert(recognition: EnqueuedRecognitionEntity): Long

    @Update
    suspend fun update(vararg recognitions: EnqueuedRecognitionEntity)

    @Query("UPDATE enqueued_recognition SET title=(:newTitle) WHERE id=(:recognitionId)")
    suspend fun updateTitle(recognitionId: Int, newTitle: String)

    @Query("SELECT record_file FROM enqueued_recognition WHERE id=(:recognitionId)")
    suspend fun getRecordingFile(recognitionId: Int): File?

    @Query("SELECT record_file FROM enqueued_recognition WHERE id in (:recognitionIds)")
    suspend fun getRecordingFiles(vararg recognitionIds: Int): List<File>

    @Query("DELETE FROM enqueued_recognition WHERE id in (:recognitionIds)")
    suspend fun delete(vararg recognitionIds: Int)

    @Query("DELETE FROM enqueued_recognition")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM enqueued_recognition WHERE id in (:recognitionIds)")
    fun getRecognitionWithTrack(vararg recognitionIds: Int): List<EnqueuedRecognitionEntityWithTrack>

    @Transaction
    @Query("SELECT * FROM enqueued_recognition WHERE id=(:recognitionId) LIMIT 1")
    fun getRecognitionWithTrackFlow(recognitionId: Int): Flow<EnqueuedRecognitionEntityWithTrack?>

    @Transaction
    @Query("SELECT * FROM enqueued_recognition ORDER BY creation_date DESC")
    fun getAllRecognitionsWithTrackFlow(): Flow<List<EnqueuedRecognitionEntityWithTrack>>
}
