package com.mrsep.musicrecognizer.glue.developermode.adapter

import com.mrsep.musicrecognizer.data.track.TrackDataRepository
import com.mrsep.musicrecognizer.data.track.util.DatabaseFiller
import com.mrsep.musicrecognizer.feature.developermode.domain.DatabaseInteractor
import javax.inject.Inject

class AdapterDatabaseInteractor @Inject constructor(
    private val databaseFiller: DatabaseFiller,
    private val trackDataRepository: TrackDataRepository
) : DatabaseInteractor {

    override suspend fun clear() {
        trackDataRepository.deleteAll()
    }

    override suspend fun prepopulateByFaker(count: Int) {
        databaseFiller.prepopulateByFaker(count)
    }

    override suspend fun prepopulateFromAssets() {
        databaseFiller.prepopulateFromAssets()
    }
}