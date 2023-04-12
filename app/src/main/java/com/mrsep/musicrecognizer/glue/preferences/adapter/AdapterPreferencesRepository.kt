package com.mrsep.musicrecognizer.glue.preferences.adapter

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesDataRepository
import com.mrsep.musicrecognizer.feature.preferences.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesDataRepository: PreferencesDataRepository,
    private val preferencesToDomainMapper: Mapper<UserPreferencesProto, UserPreferences>,
    private val requiredServicesToProtoMapper: Mapper<UserPreferences.RequiredServices, UserPreferencesProto.RequiredServicesProto>
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesDataRepository.userPreferencesFlow
            .map { proto -> preferencesToDomainMapper.map(proto) }

    override suspend fun saveApiToken(newToken: String) {
        preferencesDataRepository.saveApiToken(newToken)
    }

    override suspend fun setOnboardingCompleted(value: Boolean) {
        preferencesDataRepository.setOnboardingCompleted(value)
    }

    override suspend fun setNotificationServiceEnabled(value: Boolean) {
        preferencesDataRepository.setNotificationServiceEnabled(value)
    }

    override suspend fun setDynamicColorsEnabled(value: Boolean) {
        preferencesDataRepository.setDynamicColorsEnabled(value)
    }

    override suspend fun setDeveloperModeEnabled(value: Boolean) {
        preferencesDataRepository.setDeveloperModeEnabled(value)
    }

    override suspend fun setRequiredServices(requiredServices: UserPreferences.RequiredServices) {
        preferencesDataRepository.setRequiredServices(
            requiredServicesToProtoMapper.map(requiredServices)
        )
    }

}