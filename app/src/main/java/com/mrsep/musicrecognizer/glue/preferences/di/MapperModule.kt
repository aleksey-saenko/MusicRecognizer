package com.mrsep.musicrecognizer.glue.preferences.di

import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.UserPreferencesProto.*
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.feature.preferences.domain.ScheduleAction
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.glue.preferences.mapper.PreferencesToDomainMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.RequiredServicesMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.ScheduleActionMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.SchedulePolicyMapper
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
    fun bindRequiredServicesMapper(implementation: RequiredServicesMapper):
            BidirectionalMapper<RequiredServicesProto, UserPreferences.RequiredServices>

    @Binds
    fun bindScheduleActionMapper(implementation: ScheduleActionMapper):
            BidirectionalMapper<ScheduleActionProto, ScheduleAction>

    @Binds
    fun bindSchedulePolicyMapper(implementation: SchedulePolicyMapper):
            BidirectionalMapper<SchedulePolicyProto, UserPreferences.SchedulePolicy>


}