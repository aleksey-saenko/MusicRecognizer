package com.mrsep.musicrecognizer.glue.preferences.di

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo.*
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.AuddConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import com.mrsep.musicrecognizer.feature.preferences.domain.AcrCloudConfig
import com.mrsep.musicrecognizer.feature.preferences.domain.AuddConfig
import com.mrsep.musicrecognizer.feature.preferences.domain.FallbackAction
import com.mrsep.musicrecognizer.feature.preferences.domain.MusicService
import com.mrsep.musicrecognizer.feature.preferences.domain.RecognitionProvider
import com.mrsep.musicrecognizer.feature.preferences.domain.ThemeMode
import com.mrsep.musicrecognizer.feature.preferences.domain.UserPreferences
import com.mrsep.musicrecognizer.glue.preferences.mapper.AcrCloudConfigMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.AuddConfigMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.FallbackActionMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.FallbackPolicyMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.HapticFeedbackMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.MusicServiceMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.PreferencesMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.RecognitionProviderMapper
import com.mrsep.musicrecognizer.glue.preferences.mapper.ThemeModeMapper
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
    fun bindMusicServiceMapper(implementation: MusicServiceMapper):
            BidirectionalMapper<MusicServiceDo, MusicService>

    @Binds
    fun bindFallbackActionMapper(implementation: FallbackActionMapper):
            BidirectionalMapper<FallbackActionDo, FallbackAction>

    @Binds
    fun bindFallbackPolicyMapper(implementation: FallbackPolicyMapper):
            BidirectionalMapper<FallbackPolicyDo, UserPreferences.FallbackPolicy>

    @Binds
    fun bindHapticFeedbackMapper(implementation: HapticFeedbackMapper):
            BidirectionalMapper<HapticFeedbackDo, UserPreferences.HapticFeedback>

    @Binds
    fun bindThemeModeMapper(implementation: ThemeModeMapper):
            BidirectionalMapper<ThemeModeDo, ThemeMode>

    @Binds
    fun bindRecognitionProviderMapper(implementation: RecognitionProviderMapper):
            BidirectionalMapper<RecognitionProviderDo, RecognitionProvider>

    @Binds
    fun bindAuddConfigMapper(implementation: AuddConfigMapper):
            BidirectionalMapper<AuddConfigDo, AuddConfig>

    @Binds
    fun bindAcrCloudConfigMapper(implementation: AcrCloudConfigMapper):
            BidirectionalMapper<AcrCloudConfigDo, AcrCloudConfig>
}
