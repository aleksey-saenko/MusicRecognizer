package com.mrsep.musicrecognizer.data.track

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.mrsep.musicrecognizer.data.database.DatabaseUtils.eachDbChunk
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TrackDao {

    @Upsert
    suspend fun upsert(tracks: List<TrackEntity>)

    @Update
    suspend fun update(tracks: List<TrackEntity>)

    @Transaction
    suspend fun upsertKeepProperties(tracks: List<TrackEntity>): List<TrackEntity> {
        val trackList = copyKeepProperties(tracks)
        upsert(trackList)
        return trackList
    }

    @Transaction
    suspend fun updateKeepProperties(tracks: List<TrackEntity>) {
        val trackList = copyKeepProperties(tracks)
        update(trackList)
    }

    @Transaction
    suspend fun delete(trackIds: List<String>) {
        trackIds.eachDbChunk(::deleteInternal)
    }

    @Query("DELETE FROM track WHERE id in (:trackIds)")
    suspend fun deleteInternal(trackIds: List<String>)

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
        """
            SELECT id, title, artist, album, recognition_date, link_artwork_thumb, link_artwork, is_viewed FROM track WHERE 
                CASE 
                    WHEN 'Title' IN (:searchScope) 
                    THEN title LIKE (:pattern) ESCAPE (:escapeSymbol) 
                    ELSE 0 
                END 
                OR CASE 
                    WHEN 'Artist' IN (:searchScope) 
                    THEN artist LIKE (:pattern) ESCAPE (:escapeSymbol) 
                    ELSE 0 
                END 
                OR CASE 
                    WHEN 'Album' IN (:searchScope) 
                    THEN album LIKE (:pattern) ESCAPE (:escapeSymbol) 
                    ELSE 0 
                END 
            ORDER BY recognition_date DESC
        """
    )
    fun getPreviewsFlowByPattern(
        pattern: String,
        escapeSymbol: String,
        searchScope: Set<TrackDataFieldDo>
    ): Flow<List<TrackPreview>>

    @RawQuery(observedEntities = [TrackEntity::class])
    fun getPreviewsFlowByQuery(query: SupportSQLiteQuery): Flow<List<TrackPreview>>

    private suspend fun copyKeepProperties(tracks: List<TrackEntity>): List<TrackEntity> {
        return tracks.map { newTrack ->
            val oldTrack = getTrack(newTrack.id) ?: return@map newTrack
            newTrack.copy(properties = oldTrack.properties)
        }
    }
}
