package com.mrsep.musicrecognizer.data.enqueued

import androidx.room.*
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntityWithTrack
import kotlinx.coroutines.flow.Flow
import java.io.File

@Dao
interface EnqueuedRecognitionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(enqueued: EnqueuedRecognitionEntity): Long

    @Update
    suspend fun update(vararg enqueued: EnqueuedRecognitionEntity)

    @Query("UPDATE enqueued_recognition SET title=(:newTitle) WHERE id=(:id)")
    suspend fun updateTitle(id: Int, newTitle: String)

    @Query("SELECT record_file FROM enqueued_recognition WHERE id=(:id)")
    suspend fun getRecordingFile(id: Int): File?

    @Query("SELECT record_file FROM enqueued_recognition WHERE id in (:id)")
    suspend fun getRecordingFiles(vararg id: Int): List<File>

    @Query("DELETE FROM enqueued_recognition WHERE id in (:id)")
    suspend fun deleteById(vararg id: Int)

    @Query("DELETE FROM enqueued_recognition")
    suspend fun deleteAll()

    @Query("SELECT * FROM enqueued_recognition WHERE id=(:id)")
    suspend fun getById(id: Int): EnqueuedRecognitionEntity?

    @Query("SELECT * FROM enqueued_recognition WHERE id in (:id)")
    suspend fun getByIds(vararg id: Int): List<EnqueuedRecognitionEntity>

    @Query("SELECT * FROM enqueued_recognition WHERE id=(:id) LIMIT 1")
    fun getFlowById(id: Int): Flow<EnqueuedRecognitionEntity?>

    @Query("SELECT * FROM enqueued_recognition ORDER BY creation_date DESC")
    fun getFlowAll(): Flow<List<EnqueuedRecognitionEntity>>

    @Transaction
    @Query("SELECT * FROM enqueued_recognition WHERE id in (:id)")
    fun getByIdWithOptionalTrack(vararg id: Int): List<EnqueuedRecognitionEntityWithTrack>

    @Transaction
    @Query("SELECT * FROM enqueued_recognition")
    fun getAllWithOptionalTrackAll(): List<EnqueuedRecognitionEntityWithTrack>

    @Transaction
    @Query("SELECT * FROM enqueued_recognition WHERE id=(:id) LIMIT 1")
    fun getFlowByIdWithOptionalTrack(id: Int): Flow<EnqueuedRecognitionEntityWithTrack?>

    @Transaction
    @Query("SELECT * FROM enqueued_recognition ORDER BY creation_date DESC")
    fun getFlowAllWithOptionalTrack(): Flow<List<EnqueuedRecognitionEntityWithTrack>>

}
