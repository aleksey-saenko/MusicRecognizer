package com.mrsep.musicrecognizer.data.track

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.mrsep.musicrecognizer.domain.model.Track
import kotlinx.coroutines.delay

class TrackPagingSource(
    private val loadTracks: suspend (pageIndex: Int, pageSize: Int) -> List<Track>,
    private val pageSize: Int //need for handle first 3x load
) : PagingSource<Int, Track>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Track> {
        Log.d("TrackPagingSource params", "loadSize=${params.loadSize}, key=${params.key}")
        return try {
            val nextPageNumber = params.key ?: 0
            val response = loadTracks(nextPageNumber, params.loadSize)
            delay(3000)
            LoadResult.Page(
                data = response,
                prevKey = if (nextPageNumber == 0) null else nextPageNumber - 1,
                // handle case of first load (params.loadSize x3 times larger)
                nextKey = if (response.size == params.loadSize) nextPageNumber + (params.loadSize / pageSize) else null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(
                throwable = e
            )
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Track>): Int? {
//        Log.d("getRefreshKey", "anchorPosition=${state.anchorPosition}, closestPage=${state.anchorPosition?.let { state.closestPageToPosition(it) }}")
//        // get the most recently accessed index in the users list:
//        val anchorPosition = state.anchorPosition ?: return null
//        // convert item index to page index:
//        val page = state.closestPageToPosition(anchorPosition) ?: return null
//        // page doesn't have 'currentKey' property, so need to calculate it manually:
//        val a =  page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
//        Log.d("getRefreshKey", "return=$a")
//        return a
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    }

}
