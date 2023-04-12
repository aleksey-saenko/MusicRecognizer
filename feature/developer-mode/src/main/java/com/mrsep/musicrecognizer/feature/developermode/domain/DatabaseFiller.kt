package com.mrsep.musicrecognizer.feature.developermode.domain

interface DatabaseFiller {

    suspend fun prepopulateByFaker(count: Int)
    suspend fun prepopulateFromAssets()

}