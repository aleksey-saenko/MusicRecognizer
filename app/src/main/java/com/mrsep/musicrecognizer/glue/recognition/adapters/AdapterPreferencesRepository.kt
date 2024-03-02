package com.mrsep.musicrecognizer.glue.recognition.adapters

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.recognition.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.recognition.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesRepositoryDo: PreferencesRepositoryDo,
    private val userPreferencesMapper: Mapper<UserPreferencesDo, UserPreferences>
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesRepositoryDo.userPreferencesFlow
            .map { prefData -> userPreferencesMapper.map(prefData) }

    override suspend fun setNotificationServiceEnabled(value: Boolean) {
        preferencesRepositoryDo.setNotificationServiceEnabled(value)
    }

    override suspend fun setUseGridForQueue(value: Boolean) {
        preferencesRepositoryDo.setUseGridForRecognitionQueue(value)
    }

    override suspend fun setShowCreationDateInQueue(value: Boolean) {
        preferencesRepositoryDo.setShowCreationDateInQueue(value)
    }

}