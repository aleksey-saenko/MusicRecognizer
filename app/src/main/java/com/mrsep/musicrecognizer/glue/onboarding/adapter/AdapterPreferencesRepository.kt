package com.mrsep.musicrecognizer.glue.onboarding.adapter

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.onboarding.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesRepositoryDo: PreferencesRepositoryDo,
    private val preferencesMapper: Mapper<UserPreferencesDo, UserPreferences>
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesRepositoryDo.userPreferencesFlow
            .map { prefData -> preferencesMapper.map(prefData) }

    override suspend fun saveApiToken(newToken: String) {
        preferencesRepositoryDo.saveApiToken(newToken)
    }

    override suspend fun setOnboardingCompleted(value: Boolean) {
        preferencesRepositoryDo.setOnboardingCompleted(value)
    }

}