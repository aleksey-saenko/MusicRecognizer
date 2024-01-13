package com.mrsep.musicrecognizer.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.mrsep.musicrecognizer.MusicServiceProto
import com.mrsep.musicrecognizer.UserPreferencesProto
import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.core.common.di.ApplicationScope
import com.mrsep.musicrecognizer.core.common.di.IoDispatcher
import com.mrsep.musicrecognizer.data.preferences.FontSizeDo
import com.mrsep.musicrecognizer.data.preferences.FallbackActionDo
import com.mrsep.musicrecognizer.data.preferences.RequiredMusicServicesMigration
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesProtoSerializer
import com.mrsep.musicrecognizer.data.preferences.mappers.FontSizeDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.LyricsFontStyleDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.FallbackActionDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.FallbackPolicyDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.HapticFeedbackDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.MusicServiceDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.ThemeModeDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.TrackFilterDoMapper
import com.mrsep.musicrecognizer.data.preferences.mappers.UserPreferencesDoMapper
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

private const val USER_PREFERENCES_STORE = "USER_PREFERENCES_STORE"

@Module
@InstallIn(SingletonComponent::class)
class PreferencesModule {

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
interface PreferencesMappersModule {

    @Binds
    fun bindUserPreferencesDoMapper(implementation: UserPreferencesDoMapper):
            Mapper<UserPreferencesProto, UserPreferencesDo>

    @Binds
    fun bindMusicServiceDoMapper(implementation: MusicServiceDoMapper):
            BidirectionalMapper<MusicServiceProto?, MusicServiceDo?>

    @Binds
    fun bindFallbackActionDoMapper(implementation: FallbackActionDoMapper):
            BidirectionalMapper<UserPreferencesProto.FallbackActionProto, FallbackActionDo>

    @Binds
    fun bindFallbackPolicyDoMapper(implementation: FallbackPolicyDoMapper):
            BidirectionalMapper<UserPreferencesProto.FallbackPolicyProto, UserPreferencesDo.FallbackPolicyDo>

    @Binds
    fun bindFontSizeDoMapper(implementation: FontSizeDoMapper):
            BidirectionalMapper<UserPreferencesProto.FontSizeProto, FontSizeDo>

    @Binds
    fun bindLyricsFontStyleDoMapper(implementation: LyricsFontStyleDoMapper):
            BidirectionalMapper<UserPreferencesProto.LyricsFontStyleProto, UserPreferencesDo.LyricsFontStyleDo>

    @Binds
    fun bindTrackFilterDoMapper(implementation: TrackFilterDoMapper):
            BidirectionalMapper<UserPreferencesProto.TrackFilterProto, UserPreferencesDo.TrackFilterDo>

    @Binds
    fun bindHapticFeedbackDoMapper(implementation: HapticFeedbackDoMapper):
            BidirectionalMapper<UserPreferencesProto.HapticFeedbackProto, UserPreferencesDo.HapticFeedbackDo>

    @Binds
    fun bindThemeModeDoMapper(implementation: ThemeModeDoMapper):
            BidirectionalMapper<UserPreferencesProto.ThemeModeProto, ThemeModeDo>

}