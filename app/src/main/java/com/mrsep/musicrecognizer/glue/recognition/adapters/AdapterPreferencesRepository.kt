package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesDataRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesDataRepository: PreferencesDataRepository,
    private val userPreferencesMapper: Mapper<UserPreferencesProto, UserPreferences>
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesDataRepository.userPreferencesFlow
            .map { proto -> userPreferencesMapper.map(proto) }

}