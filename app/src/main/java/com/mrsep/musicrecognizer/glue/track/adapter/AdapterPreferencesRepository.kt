package com.mrsep.musicrecognizer.glue.track.adapter

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesDataRepository
import com.mrsep.musicrecognizer.feature.track.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesDataRepository: PreferencesDataRepository,
    private val preferencesToDomainMapper: Mapper<UserPreferencesProto, UserPreferences>
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesDataRepository.userPreferencesFlow
            .map { proto -> preferencesToDomainMapper.map(proto) }

}