package com.mrsep.musicrecognizer.di

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryDo
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.domain.PreferencesRepository
import com.mrsep.musicrecognizer.domain.ThemeMode
import com.mrsep.musicrecognizer.domain.UserPreferences
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface PreferencesModule {

    @Binds
    fun bindPreferencesMapper(implementation: PreferencesMapper):
            Mapper<UserPreferencesDo, UserPreferences>

    @Binds
    fun bindPreferencesRepository(implementation: AdapterPreferencesRepository):
            PreferencesRepository
}

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesRepositoryDo: PreferencesRepositoryDo,
    private val preferencesMapper: Mapper<UserPreferencesDo, UserPreferences>
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesRepositoryDo.userPreferencesFlow
            .map { preferencesMapper.map(it) }

    override suspend fun setNotificationServiceEnabled(value: Boolean) {
        preferencesRepositoryDo.setNotificationServiceEnabled(value)
    }
}

class PreferencesMapper @Inject constructor() :
    Mapper<UserPreferencesDo, UserPreferences> {

    override fun map(input: UserPreferencesDo): UserPreferences {
        return UserPreferences(
            onboardingCompleted = input.onboardingCompleted,
            notificationServiceEnabled = input.notificationServiceEnabled,
            themeMode = when (input.themeMode) {
                ThemeModeDo.FollowSystem -> ThemeMode.FollowSystem
                ThemeModeDo.AlwaysLight -> ThemeMode.AlwaysLight
                ThemeModeDo.AlwaysDark -> ThemeMode.AlwaysDark
            },
            dynamicColorsEnabled = input.dynamicColorsEnabled,
            usePureBlackForDarkTheme = input.usePureBlackForDarkTheme,
            developerModeEnabled = input.developerModeEnabled,
            recognizeOnStartup = input.recognizeOnStartup
        )
    }
}
