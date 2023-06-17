package com.mrsep.musicrecognizer.glue.track.adapter

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.PreferencesRepositoryDo
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.track.domain.PreferencesRepository
import com.mrsep.musicrecognizer.feature.track.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AdapterPreferencesRepository @Inject constructor(
    private val preferencesRepositoryDo: PreferencesRepositoryDo,
    private val preferencesMapper: Mapper<UserPreferencesDo, UserPreferences>,
    private val lyricsFontStyleMapper: BidirectionalMapper<UserPreferencesDo.LyricsFontStyleDo, UserPreferences.LyricsFontStyle>
) : PreferencesRepository {

    override val userPreferencesFlow: Flow<UserPreferences>
        get() = preferencesRepositoryDo.userPreferencesFlow
            .map { prefData -> preferencesMapper.map(prefData) }

    override suspend fun setLyricsFontStyle(value: UserPreferences.LyricsFontStyle) {
        preferencesRepositoryDo.setLyricsFontStyle(lyricsFontStyleMapper.reverseMap(value))
    }

}