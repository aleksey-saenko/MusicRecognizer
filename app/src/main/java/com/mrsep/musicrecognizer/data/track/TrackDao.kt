package com.mrsep.musicrecognizer.data.track

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(vararg track: TrackEntity)

    @Query("SELECT * FROM track WHERE mb_id=(:mbId) LIMIT 1")
    suspend fun getByMbId(mbId: String): TrackEntity?

    @Query("SELECT * FROM track WHERE mb_id=(:mbId) LIMIT 1")
    fun getByMbIdFlow(mbId: String): Flow<TrackEntity?>

    @Query("SELECT * FROM track WHERE last_recognition_date>=(:date) LIMIT (:limit)")
    suspend fun getAfterDate(date: Long, limit: Int): List<TrackEntity>

    @Query("SELECT * FROM track ORDER BY last_recognition_date DESC LIMIT (:limit)")
    suspend fun getLastRecognized(limit: Int): List<TrackEntity>

    @Query("SELECT * FROM track ORDER BY last_recognition_date DESC LIMIT (:limit)")
    fun getLastRecognizedFlow(limit: Int): Flow<List<TrackEntity>>



}