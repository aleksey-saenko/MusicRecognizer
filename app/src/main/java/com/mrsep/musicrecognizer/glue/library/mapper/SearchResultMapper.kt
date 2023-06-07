package com.mrsep.musicrecognizer.glue.library.mapper

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.track.SearchResultDo
import com.mrsep.musicrecognizer.feature.library.domain.model.SearchResult
import com.mrsep.musicrecognizer.feature.library.domain.model.Track
import javax.inject.Inject

class SearchResultMapper @Inject constructor() :
    Mapper<SearchResultDo<@JvmSuppressWildcards Track>, SearchResult<@JvmSuppressWildcards Track>> {

    override fun map(input: SearchResultDo<Track>): SearchResult<Track> {
        return when (input) {
            is SearchResultDo.Pending -> SearchResult.Pending(input.keyword)
            is SearchResultDo.Success -> SearchResult.Success(input.keyword, input.data)
        }
    }

}