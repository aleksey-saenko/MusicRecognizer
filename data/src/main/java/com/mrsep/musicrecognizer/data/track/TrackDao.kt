package com.mrsep.musicrecognizer.data.track

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TrackDao {

    @Upsert
    suspend fun upsert(vararg tracks: TrackEntity)

    @Update
    suspend fun update(vararg tracks: TrackEntity)

    @Transaction
    suspend fun upsertKeepProperties(vararg tracks: TrackEntity): List<TrackEntity> {
        val trackList = copyKeepProperties(*tracks)
        upsert(*trackList.toTypedArray())
        return trackList
    }

    @Transaction
    suspend fun updateKeepProperties(vararg tracks: TrackEntity) {
        val trackList = copyKeepProperties(*tracks)
        update(*trackList.toTypedArray())
    }

    @Query("DELETE FROM track WHERE id in (:trackIds)")
    suspend fun delete(vararg trackIds: String)

    @Query("DELETE FROM track")
    suspend fun deleteAll()

    @Query("UPDATE track SET is_favorite=(:isFavorite) WHERE id=(:trackId)")
    suspend fun setFavorite(trackId: String, isFavorite: Boolean)

    @Query("UPDATE track SET is_viewed=(:isViewed) WHERE id=(:trackId)")
    suspend fun setViewed(trackId: String, isViewed: Boolean)

    @Query("UPDATE track SET theme_seed_color=(:color) WHERE id=(:trackId)")
    suspend fun setThemeSeedColor(trackId: String, color: Int?)

    @Query("SELECT (SELECT COUNT(*) FROM track) == 0")
    fun isEmptyDatabaseFlow(): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM track WHERE is_viewed=0")
    fun getUnviewedCountFlow(): Flow<Int>

    @Query("SELECT * FROM track WHERE id=(:trackId) LIMIT 1")
    suspend fun getTrack(trackId: String): TrackEntity?

    @Query("SELECT * FROM track WHERE id=(:trackId) LIMIT 1")
    fun getTrackFlow(trackId: String): Flow<TrackEntity?>

    @Query(
        "SELECT * FROM track " +
                "WHERE title LIKE (:key) OR artist LIKE (:key) OR album LIKE (:key)" +
                "ESCAPE (:escapeSymbol) " +
                "ORDER BY recognition_date DESC " +
                "LIMIT (:limit)"
    )
    fun getTracksFlowByKeyword(
        key: String,
        escapeSymbol: String,
        limit: Int
    ): Flow<List<TrackEntity>>

    @RawQuery(observedEntities = [TrackEntity::class])
    fun getTracksFlowByQuery(query: SupportSQLiteQuery): Flow<List<TrackEntity>>


    private suspend fun copyKeepProperties(vararg tracks: TrackEntity): List<TrackEntity> {
        return tracks.map { newTrack ->
            getTrack(newTrack.id)?.let { oldTrack ->
                newTrack.copy(properties = oldTrack.properties)
            } ?: newTrack
        }
    }

}