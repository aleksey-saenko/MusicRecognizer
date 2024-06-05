package com.mrsep.musicrecognizer.glue.library.adapter

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.feature.library.domain.model.UserPreferences
import com.mrsep.musicrecognizer.feature.library.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesMapper: Mapper<UserPreferencesDo, UserPreferences>,
    private val trackFilterMapper: BidirectionalMapper<UserPreferencesDo.TrackFilterDo, TrackFilter>,
    private val preferencesRepositoryDo: PreferencesRepositoryDo
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesRepositoryDo.userPreferencesFlow.map(preferencesMapper::map)

    override suspend fun setTrackFilter(value: TrackFilter) {
        preferencesRepositoryDo.setTrackFilter(trackFilterMapper.reverseMap(value))
    }

    override suspend fun setUseGridForLibrary(value: Boolean) {
        preferencesRepositoryDo.setUseGridForLibrary(value)
    }

    override suspend fun setShowRecognitionDateInLibrary(value: Boolean) {
        preferencesRepositoryDo.setShowRecognitionDateInLibrary(value)
    }
}
