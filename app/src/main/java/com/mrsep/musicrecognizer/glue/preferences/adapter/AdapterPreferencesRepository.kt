package com.mrsep.musicrecognizer.glue.preferences.adapter

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesRepositoryDo: PreferencesRepositoryDo,
    private val preferencesMapper: Mapper<UserPreferencesDo, UserPreferences>,
    private val requiredServicesMapper: BidirectionalMapper<RequiredServicesDo, RequiredServices>,
    private val schedulePolicyMapper: BidirectionalMapper<SchedulePolicyDo, SchedulePolicy>
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesRepositoryDo.userPreferencesFlow
            .map { prefData -> preferencesMapper.map(prefData) }

    override suspend fun saveApiToken(newToken: String) {
        preferencesRepositoryDo.setApiToken(newToken)
    }

    override suspend fun setOnboardingCompleted(value: Boolean) {
        preferencesRepositoryDo.setOnboardingCompleted(value)
    }

    override suspend fun setNotificationServiceEnabled(value: Boolean) {
        preferencesRepositoryDo.setNotificationServiceEnabled(value)
    }

    override suspend fun setDynamicColorsEnabled(value: Boolean) {
        preferencesRepositoryDo.setDynamicColorsEnabled(value)
    }

    override suspend fun setDeveloperModeEnabled(value: Boolean) {
        preferencesRepositoryDo.setDeveloperModeEnabled(value)
    }

    override suspend fun setRequiredServices(requiredServices: RequiredServices) {
        preferencesRepositoryDo.setRequiredServices(
            requiredServicesMapper.reverseMap(requiredServices)
        )
    }

    override suspend fun setSchedulePolicy(schedulePolicy: SchedulePolicy) {
        preferencesRepositoryDo.setSchedulePolicy(
            schedulePolicyMapper.reverseMap(schedulePolicy)
        )
    }

}