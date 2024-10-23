package com.mrsep.musicrecognizer.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.mrsep.musicrecognizer.AcrCloudConfigProto
import com.mrsep.musicrecognizer.AudioCaptureModeProto
import com.mrsep.musicrecognizer.MusicServiceProto
import com.mrsep.musicrecognizer.RecognitionProviderProto
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.preferences.AudioCaptureModeDo
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.data.preferences.FontSizeDo
import com.mrsep.musicrecognizer.data.preferences.RequiredMusicServicesMigration
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesProtoSerializer
import com.mrsep.musicrecognizer.data.preferences.mappers.AcrCloudConfigDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.AudioCaptureModeDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.FallbackActionDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.FallbackPolicyDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.FontSizeDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.HapticFeedbackDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.LyricsFontStyleDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.MusicServiceDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.RecognitionProviderDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.ThemeModeDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.TrackFilterDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.UserPreferencesDoMapper
import com.mrsep.musicrecognizer.data.remote.AcrCloudConfigDo
import com.mrsep.musicrecognizer.data.remote.RecognitionProviderDo
import com.mrsep.musicrecognizer.data.track.MusicServiceDo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

internal const val USER_PREFERENCES_STORE = "USER_PREFERENCES_STORE"

@Module
@InstallIn(SingletonComponent::class)
internal object PreferencesModule {

    @Provides
    @Singleton
    fun provideUserPreferences(
        @ApplicationContext appContext: Context,
        @ApplicationScope appScope: CoroutineScope,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): DataStore<UserPreferencesProto> {
        return DataStoreFactory.create(
            serializer = UserPreferencesProtoSerializer,
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { UserPreferencesProto.getDefaultInstance() }
            ),
            migrations = listOf(
                RequiredMusicServicesMigration,
            ),
            scope = CoroutineScope(appScope.coroutineContext + ioDispatcher),
            produceFile = { appContext.dataStoreFile(USER_PREFERENCES_STORE) }
        )
    }
}

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
internal interface PreferencesMappersModule {

    @Binds
    fun bindUserPreferencesDoMapper(impl: UserPreferencesDoMapper):
            Mapper<UserPreferencesProto, UserPreferencesDo>

    @Binds
    fun bindMusicServiceDoMapper(impl: MusicServiceDoMapper):
            BidirectionalMapper<MusicServiceProto?, MusicServiceDo?>

    @Binds
    fun bindFallbackActionDoMapper(impl: FallbackActionDoMapper):
            BidirectionalMapper<UserPreferencesProto.FallbackActionProto, FallbackActionDo>

    @Binds
    fun bindFallbackPolicyDoMapper(impl: FallbackPolicyDoMapper):
            BidirectionalMapper<UserPreferencesProto.FallbackPolicyProto, UserPreferencesDo.FallbackPolicyDo>

    @Binds
    fun bindFontSizeDoMapper(impl: FontSizeDoMapper):
            BidirectionalMapper<UserPreferencesProto.FontSizeProto, FontSizeDo>

    @Binds
    fun bindLyricsFontStyleDoMapper(impl: LyricsFontStyleDoMapper):
            BidirectionalMapper<UserPreferencesProto.LyricsFontStyleProto, UserPreferencesDo.LyricsFontStyleDo>

    @Binds
    fun bindTrackFilterDoMapper(impl: TrackFilterDoMapper):
            BidirectionalMapper<UserPreferencesProto.TrackFilterProto, UserPreferencesDo.TrackFilterDo>

    @Binds
    fun bindHapticFeedbackDoMapper(impl: HapticFeedbackDoMapper):
            BidirectionalMapper<UserPreferencesProto.HapticFeedbackProto, UserPreferencesDo.HapticFeedbackDo>

    @Binds
    fun bindThemeModeDoMapper(impl: ThemeModeDoMapper):
            BidirectionalMapper<UserPreferencesProto.ThemeModeProto, ThemeModeDo>

    @Binds
    fun bindAcrCloudConfigDoMapper(impl: AcrCloudConfigDoMapper):
            BidirectionalMapper<AcrCloudConfigProto, AcrCloudConfigDo>

    @Binds
    fun bindRecognitionProviderDoMapper(impl: RecognitionProviderDoMapper):
            BidirectionalMapper<RecognitionProviderProto, RecognitionProviderDo>

    @Binds
    fun bindAudioCaptureModeDoMapper(impl: AudioCaptureModeDoMapper):
            BidirectionalMapper<AudioCaptureModeProto, AudioCaptureModeDo>
}
