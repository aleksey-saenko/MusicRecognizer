package com.mrsep.musicrecognizer.glue.library.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.SearchDataResult
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import javax.inject.Inject

class SearchResultToDomainMapper @Inject constructor() :
    Mapper<SearchDataResult<@JvmSuppressWildcards Track>, SearchResult<@JvmSuppressWildcards Track>> {

    override fun map(input: SearchDataResult<Track>): SearchResult<Track> {
        return when (input) {
            is SearchDataResult.Pending -> SearchResult.Pending(input.keyword)
            is SearchDataResult.Success -> SearchResult.Success(input.keyword, input.data)
        }
    }

}