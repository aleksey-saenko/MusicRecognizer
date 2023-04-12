package com.mrsep.musicrecognizer.data.enqueued

import androidx.room.*
import com.mrsep.musicrecognizer.data.enqueued.model.EnqueuedRecognitionEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

@Dao
interface EnqueuedRecognitionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(enqueued: EnqueuedRecognitionEntity): Long

    @Update
    suspend fun update(enqueued: EnqueuedRecognitionEntity)

    @Query("UPDATE enqueued_recognition SET title=(:newTitle) WHERE id=(:id)")
    suspend fun updateTitle(id: Int, newTitle: String)

    @Query("SELECT record_file FROM enqueued_recognition WHERE id=(:id)")
    suspend fun getFileRecord(id: Int): File?

    @Query("DELETE FROM enqueued_recognition WHERE id=(:id)")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM enqueued_recognition")
    suspend fun deleteAll()

    @Query("SELECT * FROM enqueued_recognition WHERE id=(:id)")
    suspend fun getById(id: Int): EnqueuedRecognitionEntity?

    @Query("SELECT * FROM enqueued_recognition WHERE id=(:id) LIMIT 1")
    fun getUniqueFlow(id: Int): Flow<EnqueuedRecognitionEntity?>

    @Query("SELECT * FROM enqueued_recognition ORDER BY creation_date DESC LIMIT (:limit)")
    fun getFlow(limit: Int): Flow<List<EnqueuedRecognitionEntity>>

}
