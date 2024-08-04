package com.mrsep.musicrecognizer.data.database

internal object DatabaseUtils {

    // Workaround for https://issuetracker.google.com/issues/73634057
    // This has limitations, for example when selecting with ordering. Use with caution.
    private const val MAX_SQLITE_ARGS = 990

    suspend fun <T> Iterable<T>.eachDbChunk(action: suspend (List<T>) -> Unit) =
        dbChunked().forEach { action(it) }

    suspend fun <T, R> Iterable<T>.dbChunkedMap(transform: suspend (List<T>) -> Iterable<R>): List<R> =
        dbChunked().flatMap { transform(it) }

    private fun <T> Iterable<T>.dbChunked(): List<List<T>> = chunked(MAX_SQLITE_ARGS)
}