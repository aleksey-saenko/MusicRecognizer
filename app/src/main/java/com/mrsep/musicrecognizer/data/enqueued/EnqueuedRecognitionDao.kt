package com.mrsep.musicrecognizer.data.enqueued

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EnqueuedRecognitionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(enqueued: EnqueuedRecognitionEntity): Long

    @Update
    suspend fun update(enqueued: EnqueuedRecognitionEntity)

    @Query("DELETE FROM enqueued_recognition WHERE id=(:id)")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM enqueued_recognition")
    suspend fun deleteAll()

    @Query("SELECT * FROM enqueued_recognition WHERE id=(:id)")
    suspend fun getById(id: Int): EnqueuedRecognitionEntity?

    @Query("SELECT * FROM enqueued_recognition ORDER BY creation_date DESC LIMIT (:limit)")
    fun getFlow(limit: Int): Flow<List<EnqueuedRecognitionEntity>>

}
