package com.mrsep.musicrecognizer.core.database.track

import androidx.room.*
import com.mrsep.musicrecognizer.core.database.DatabaseUtils
import com.mrsep.musicrecognizer.core.database.DatabaseUtils.eachDbChunk
import com.mrsep.musicrecognizer.core.database.SQLSearchPattern
import com.mrsep.musicrecognizer.core.domain.preferences.FavoritesMode
import com.mrsep.musicrecognizer.core.domain.preferences.OrderBy
import com.mrsep.musicrecognizer.core.domain.preferences.SortBy
import com.mrsep.musicrecognizer.core.domain.track.model.TrackDataField
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Upsert
    suspend fun upsert(tracks: List<TrackEntity>)

    @Update
    suspend fun update(track: TrackEntity)

    @Transaction
    suspend fun upsertKeepProperties(tracks: List<TrackEntity>): List<TrackEntity> {
        val trackList = tracks.map { newTrack ->
            val oldTrack = getTrack(newTrack.id) ?: return@map newTrack
            newTrack.copy(properties = oldTrack.properties)
        }
        upsert(trackList)
        return trackList
    }

    @Transaction
    suspend fun updateTransform(
        trackId: String,
        transform: (previous: TrackEntity) -> TrackEntity,
    ) {
        val previous = getTrack(trackId) ?: return
        val transformed = transform(previous)
        update(transformed)
    }

    @Transaction
    suspend fun delete(trackIds: List<String>) {
        trackIds.eachDbChunk(::deleteInternal)
    }

    @Query("DELETE FROM track WHERE id IN (:trackIds)")
    suspend fun deleteInternal(trackIds: List<String>)

    @Query("DELETE FROM track")
    suspend fun deleteAll()

    @Query("UPDATE track SET is_favorite = :isFavorite WHERE id = :trackId")
    suspend fun setFavorite(trackId: String, isFavorite: Boolean)

    @Query("UPDATE track SET is_viewed = :isViewed WHERE id = :trackId")
    suspend fun setViewed(trackId: String, isViewed: Boolean)

    @Query("UPDATE track SET theme_seed_color = :color WHERE id = :trackId")
    suspend fun setThemeSeedColor(trackId: String, color: Int?)

    @Query("UPDATE track SET lyrics = :lyrics, is_lyrics_synced = :isSynced WHERE id = :trackId")
    suspend fun setLyrics(trackId: String, lyrics: String, isSynced: Boolean)

    @Query("SELECT NOT EXISTS(SELECT 1 FROM track LIMIT 1)")
    fun isEmptyDatabaseFlow(): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM track WHERE is_viewed = 0")
    fun getUnviewedCountFlow(): Flow<Int>

    @Query("SELECT * FROM track WHERE id = :trackId")
    suspend fun getTrack(trackId: String): TrackEntity?

    @Query("SELECT * FROM track WHERE id = :trackId")
    fun getTrackFlow(trackId: String): Flow<TrackEntity?>

    fun getPreviewsFlowByQuery(
        query: String,
        searchScope: Set<TrackDataField>
    ): Flow<List<TrackPreviewTuple>> {
        return getPreviewsFlowByPattern(
            pattern = DatabaseUtils.createSearchPatternForSQLite(query),
            escapeSymbol = DatabaseUtils.ESCAPE_SYMBOL,
            searchScope
        )
    }

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
            SELECT * FROM track WHERE 
                CASE 
                    WHEN 'Title' IN (:searchScope) 
                    THEN title LIKE :pattern ESCAPE :escapeSymbol 
                    ELSE 0 
                END 
                OR CASE 
                    WHEN 'Artist' IN (:searchScope) 
                    THEN artist LIKE :pattern ESCAPE :escapeSymbol 
                    ELSE 0 
                END 
                OR CASE 
                    WHEN 'Album' IN (:searchScope) 
                    THEN album LIKE :pattern ESCAPE :escapeSymbol 
                    ELSE 0 
                END 
            ORDER BY recognition_date DESC
        """
    )
    fun getPreviewsFlowByPattern(
        pattern: SQLSearchPattern,
        escapeSymbol: String,
        searchScope: Set<TrackDataField>
    ): Flow<List<TrackPreviewTuple>>

    @RewriteQueriesToDropUnusedColumns
    @Query(
        """
            SELECT * FROM track 
            WHERE 
                -- Favorites filter
                CASE :favoritesMode
                    WHEN 'All' THEN 1
                    WHEN 'OnlyFavorites' THEN is_favorite
                    WHEN 'ExcludeFavorites' THEN NOT is_favorite
                END 
                AND 
                -- Date range filter
                CASE :applyDateFilter
                    WHEN 0 THEN 1
                    WHEN 1 THEN recognition_date BETWEEN :startDate AND :endDate
                END 
            ORDER BY 
                -- Sort and order
                CASE WHEN :orderBy = 'Asc' THEN
                    CASE :sortBy 
                        WHEN 'RecognitionDate' THEN recognition_date
                        WHEN 'Title' THEN title
                        WHEN 'Artist' THEN artist
                        WHEN 'ReleaseDate' THEN release_date
                    END 
                END ASC,
                CASE WHEN :orderBy = 'Desc' THEN 
                    CASE :sortBy 
                        WHEN 'RecognitionDate' THEN recognition_date
                        WHEN 'Title' THEN title
                        WHEN 'Artist' THEN artist
                        WHEN 'ReleaseDate' THEN release_date
                    END 
                END DESC
        """
    )
    fun getPreviewsFlowByFilter(
        favoritesMode: FavoritesMode,
        startDate: Long,
        endDate: Long,
        applyDateFilter: Boolean = (startDate != Long.MIN_VALUE || endDate != Long.MAX_VALUE),
        sortBy: SortBy,
        orderBy: OrderBy
    ): Flow<List<TrackPreviewTuple>>
}
