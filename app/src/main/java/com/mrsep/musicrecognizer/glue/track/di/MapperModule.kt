package com.mrsep.musicrecognizer.glue.track.di

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.ThemeModeDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.data.track.TrackEntity
import com.mrsep.musicrecognizer.feature.track.domain.model.ThemeMode
import com.mrsep.musicrecognizer.feature.track.domain.model.Track
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import com.mrsep.musicrecognizer.glue.track.mapper.*
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
    fun bindLyricsFontStyleMapper(implementation: LyricsFontStyleMapper):
            BidirectionalMapper<UserPreferencesDo.LyricsFontStyleDo, UserPreferences.LyricsFontStyle>

    @Binds
    fun bindThemeModeMapper(implementation: ThemeModeMapper):
            BidirectionalMapper<ThemeModeDo, ThemeMode>

    @Binds
    fun bindTrackMapper(implementation: TrackMapper):
            BidirectionalMapper<TrackEntity, Track>

}