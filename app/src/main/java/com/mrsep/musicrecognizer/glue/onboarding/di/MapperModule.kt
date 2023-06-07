package com.mrsep.musicrecognizer.glue.onboarding.di

import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.UserPreferences
import com.mrsep.musicrecognizer.glue.onboarding.mapper.PreferencesMapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface MapperModule {

    @Binds
    fun bindPreferencesMapper(implementation: PreferencesMapper):
            Mapper<UserPreferencesDo, UserPreferences>

}