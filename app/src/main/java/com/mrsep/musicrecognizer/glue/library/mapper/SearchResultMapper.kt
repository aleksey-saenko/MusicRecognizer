package com.mrsep.musicrecognizer.glue.library.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.SearchResultDo
import com.mrsep.musicrecognizer.data.track.TrackDataFieldDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackDataField
import javax.inject.Inject

class SearchResultMapper @Inject constructor(
    private val trackMapper: Mapper<TrackEntity, Track>,
    private val trackDataFieldMapper: BidirectionalMapper<TrackDataFieldDo, TrackDataField>,
) :
    Mapper<SearchResultDo, SearchResult> {

    override fun map(input: SearchResultDo): SearchResult {
        return when (input) {
            is SearchResultDo.Pending -> SearchResult.Pending(
                query = input.query,
                searchScope = input.searchScope.map(trackDataFieldMapper::map).toSet()
            )
            is SearchResultDo.Success -> SearchResult.Success(
                query = input.query,
                searchScope = input.searchScope.map(trackDataFieldMapper::map).toSet(),
                data = input.data.map(trackMapper::map)
            )
        }
    }

}