package com.mrsep.musicrecognizer.glue.preferences.di

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.glue.preferences.mapper.PreferencesToDomainMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.RequiredServicesToProtoMapper
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

    @Binds
    fun bindRequiredServicesToProtoMapper(implementation: RequiredServicesToProtoMapper):
            Mapper<UserPreferences.RequiredServices, UserPreferencesProto.RequiredServicesProto>

}