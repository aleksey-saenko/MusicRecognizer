package com.mrsep.musicrecognizer.data.track

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Query("SELECT (SELECT COUNT(*) FROM track) == 0")
    fun isEmptyFlow(): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(vararg track: TrackEntity)

    @Delete
    suspend fun delete(vararg track: TrackEntity)

    @Query("DELETE FROM track WHERE mb_id in (:mbId)")
    suspend fun deleteByMbId(vararg mbId: String)

    @Query("DELETE FROM track")
    suspend fun deleteAll()

    @Query("DELETE FROM track WHERE NOT is_favorite")
    suspend fun deleteAllExceptFavorites()

    @Query("DELETE FROM track WHERE is_favorite")
    suspend fun deleteAllFavorites()

    @Query("UPDATE track SET is_favorite = NOT is_favorite WHERE mb_id=(:mbId)")
    suspend fun toggleFavoriteMark(mbId: String)

    @Query("UPDATE track SET theme_seed_color=(:color) WHERE mb_id=(:mbId)")
    suspend fun updateThemeSeedColor(mbId: String, color: Int?)

    @Update
    suspend fun update(track: TrackEntity)


    @Query("SELECT COUNT(*) FROM track")
    fun countAllFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM track WHERE is_favorite")
    fun countFavoritesFlow(): Flow<Int>


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

    @Query("SELECT * FROM track WHERE NOT is_favorite ORDER BY last_recognition_date DESC LIMIT (:limit)")
    fun getNotFavoriteRecentsFlow(limit: Int): Flow<List<TrackEntity>>

    @Query("SELECT * FROM track WHERE is_favorite ORDER BY last_recognition_date DESC LIMIT (:limit)")
    fun getFavoritesFlow(limit: Int): Flow<List<TrackEntity>>

    @Query(
        "SELECT * FROM track " +
                "WHERE title LIKE (:key) OR artist LIKE (:key) OR album LIKE (:key)" +
                "ESCAPE (:escapeSymbol) " +
                "ORDER BY last_recognition_date DESC " +
                "LIMIT (:limit)"
    )
    fun search(key: String, escapeSymbol: String, limit: Int): List<TrackEntity>

    @Query(
        "SELECT * FROM track " +
                "WHERE title LIKE (:key) OR artist LIKE (:key) OR album LIKE (:key)" +
                "ESCAPE (:escapeSymbol) " +
                "ORDER BY last_recognition_date DESC " +
                "LIMIT (:limit)"
    )
    fun searchFlow(key: String, escapeSymbol: String, limit: Int): Flow<List<TrackEntity>>

    @RawQuery(observedEntities = [TrackEntity::class])
    fun getFlowByCustomQuery(query: SupportSQLiteQuery): Flow<List<TrackEntity>>

}