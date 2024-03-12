package com.mrsep.musicrecognizer.di

import com.mrsep.musicrecognizer.data.track.TrackRepositoryDo
import com.mrsep.musicrecognizer.domain.TrackRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface TrackModule {

    @Binds
    fun bindTrackRepository(implementation: AdapterTrackRepository):
            TrackRepository

}

class AdapterTrackRepository @Inject constructor(
    private val trackRepositoryDo: TrackRepositoryDo,
): TrackRepository {

    override fun getUnviewedCountFlow(): Flow<Int> {
        return trackRepositoryDo.getUnviewedCountFlow()
    }

}