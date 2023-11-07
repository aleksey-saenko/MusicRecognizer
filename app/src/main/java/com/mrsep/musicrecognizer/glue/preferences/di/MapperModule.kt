package com.mrsep.musicrecognizer.glue.preferences.di

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.feature.preferences.domain.FallbackAction
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.glue.preferences.mapper.PreferencesMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.RequiredServicesMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.FallbackActionMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.FallbackPolicyMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.HapticFeedbackMapper
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
    fun bindRequiredServicesMapper(implementation: RequiredServicesMapper):
            BidirectionalMapper<RequiredServicesDo, UserPreferences.RequiredServices>

    @Binds
    fun bindFallbackActionMapper(implementation: FallbackActionMapper):
            BidirectionalMapper<FallbackActionDo, FallbackAction>

    @Binds
    fun bindFallbackPolicyMapper(implementation: FallbackPolicyMapper):
            BidirectionalMapper<FallbackPolicyDo, UserPreferences.FallbackPolicy>

    @Binds
    fun bindHapticFeedbackMapper(implementation: HapticFeedbackMapper):
            BidirectionalMapper<HapticFeedbackDo, UserPreferences.HapticFeedback>

}