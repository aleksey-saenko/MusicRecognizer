package com.mrsep.musicrecognizer.feature.developermode.domain

interface DatabaseInteractor {

    suspend fun clear()

    suspend fun prepopulateByFaker(count: Int)

    suspend fun prepopulateFromAssets()

}