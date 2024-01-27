package com.mrsep.musicrecognizer.glue.onboarding.di

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.ConfigValidationStatusDo
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.AuddConfig
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.ConfigValidationStatus
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.UserPreferences
import com.mrsep.musicrecognizer.glue.onboarding.mapper.AuddConfigMapper
import com.mrsep.musicrecognizer.glue.onboarding.mapper.ConfigValidationStatusMapper
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

    @Binds
    fun bindTokenValidationStatusMapper(implementation: ConfigValidationStatusMapper):
            Mapper<ConfigValidationStatusDo, ConfigValidationStatus>

    @Binds
    fun bindAuddConfigMapper(implementation: AuddConfigMapper):
            BidirectionalMapper<AuddConfigDo, AuddConfig>

}