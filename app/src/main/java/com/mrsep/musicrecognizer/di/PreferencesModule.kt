package com.mrsep.musicrecognizer.di

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesDataRepository
import com.mrsep.musicrecognizer.domain.PreferencesRepository
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
    fun bindPreferencesToDomainMapper(implementation: PreferencesToDomainMapper):
            Mapper<UserPreferencesProto, UserPreferences>

    @Binds
    fun bindPreferencesRepository(implementation: AdapterPreferencesRepository):
            PreferencesRepository

}

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesDataRepository: PreferencesDataRepository,
    private val mapperPreferencesToDomain: Mapper<UserPreferencesProto, UserPreferences>
): PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesDataRepository.userPreferencesFlow
            .map { mapperPreferencesToDomain.map(it) }
}

class PreferencesToDomainMapper @Inject constructor() :
    Mapper<UserPreferencesProto, UserPreferences> {

    override fun map(input: UserPreferencesProto): UserPreferences {
        return UserPreferences(
            onboardingCompleted = input.onboardingCompleted,
            apiToken = input.apiToken,
            notificationServiceEnabled = input.notificationServiceEnabled,
            dynamicColorsEnabled = input.dynamicColorsEnabled,
            developerModeEnabled = input.developerModeEnabled,
            requiredServices = UserPreferences.RequiredServices(
                spotify = input.requiredServices.spotify,
                youtube = input.requiredServices.youtube,
                soundCloud = input.requiredServices.soundcloud,
                appleMusic = input.requiredServices.appleMusic,
                deezer = input.requiredServices.deezer,
                napster = input.requiredServices.napster,
                musicbrainz = input.requiredServices.musicbrainz
            )
        )
    }

}
