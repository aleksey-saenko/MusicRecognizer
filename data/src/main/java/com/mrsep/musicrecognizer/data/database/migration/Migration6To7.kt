package com.mrsep.musicrecognizer.data.database.migration

import androidx.core.database.getStringOrNull
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val Migration6To7 = object : Migration(6, 7) {

    private val deezerPattern by lazy {
        "^https://e-cdns-images\\.dzcdn\\.net/images/(?:cover|artist)/[-/a-z0-9]+/(\\d+)x(\\d+).*$".toRegex()
    }

    private val appleMusicPattern by lazy {
        "^https://[-.a-z0-9]*mzstatic\\.com/image/thumb/[-_./a-zA-Z0-9]+/(\\d+)x(\\d+)bb.*$".toRegex()
    }

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE track ADD COLUMN link_artwork_thumb TEXT")
        createThumbnails(db)
    }

    private fun createThumbnails(db: SupportSQLiteDatabase) {
        val cursor = db.query("SELECT id, link_artwork FROM track")
        val trackIdToThumbnailUrl = mutableListOf<Pair<String, String>>()
        if (cursor.moveToFirst()) {
            val trackIdIndex = cursor.getColumnIndex("id")
            val linkArtworkIndex = cursor.getColumnIndex("link_artwork")
            do {
                val trackId = cursor.getString(trackIdIndex)
                val artworkUrl = cursor.getStringOrNull(linkArtworkIndex) ?: continue
                val thumbnailUrl = parseDeezerThumbnailUrl(artworkUrl)
                    ?: parseAppleMusicThumbnailUrl(artworkUrl)
                    ?: continue
                trackIdToThumbnailUrl.add(trackId to thumbnailUrl)
            } while (cursor.moveToNext())
        }
        cursor.close()
        trackIdToThumbnailUrl.forEach { (trackId, thumbnailUrl) ->
            db.execSQL(
                "UPDATE track SET link_artwork_thumb = ? WHERE id = ?",
                arrayOf(thumbnailUrl, trackId)
            )
        }
    }

    private fun parseDeezerThumbnailUrl(artworkUrl: String): String? {
        val matchGroups = deezerPattern.find(artworkUrl)?.groups?.takeIf { it.size == 3 } ?: return null
        matchGroups[1]?.value?.toIntOrNull()?.takeIf { it >= 500 } ?: return null
        matchGroups[2]?.value?.toIntOrNull()?.takeIf { it >= 500 } ?: return null
        val widthRange = matchGroups[1]?.range ?: return null
        val heightRange = matchGroups[2]?.range ?: return null
        return artworkUrl.replaceRange(widthRange.first..heightRange.last, "250x250")
    }

    private fun parseAppleMusicThumbnailUrl(artworkUrl: String): String? {
        val matchGroups = appleMusicPattern.find(artworkUrl)?.groups?.takeIf { it.size == 3 } ?: return null
        matchGroups[1]?.value?.toIntOrNull()?.takeIf { it >= 500 } ?: return null
        matchGroups[2]?.value?.toIntOrNull()?.takeIf { it >= 500 } ?: return null
        val widthRange = matchGroups[1]?.range ?: return null
        val heightRange = matchGroups[2]?.range ?: return null
        return artworkUrl.replaceRange(widthRange.first..heightRange.last, "300x300")
    }
}
