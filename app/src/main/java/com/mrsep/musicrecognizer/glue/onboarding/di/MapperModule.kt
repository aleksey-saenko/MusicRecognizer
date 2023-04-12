package com.mrsep.musicrecognizer.glue.onboarding.di

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.UserPreferences
import com.mrsep.musicrecognizer.glue.onboarding.mapper.PreferencesToDomainMapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
interface MapperModule {

    @Binds
    fun bindPreferencesToDomainMapper(implementation: PreferencesToDomainMapper):
            Mapper<UserPreferencesProto, UserPreferences>

}