package com.mrsep.musicrecognizer.glue.library.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.core.common.Mapper
import com.mrsep.musicrecognizer.data.preferences.UserPreferencesDo
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackFilter
import com.mrsep.musicrecognizer.feature.library.domain.model.UserPreferences
import javax.inject.Inject

class UserPreferencesMapper @Inject constructor(
    private val trackFilterMapper: BidirectionalMapper<UserPreferencesDo.TrackFilterDo, TrackFilter>
) : Mapper<UserPreferencesDo, UserPreferences> {

    override fun map(input: UserPreferencesDo): UserPreferences {
        return UserPreferences(
            trackFilter = trackFilterMapper.map(input.trackFilter),
            useGridForLibrary = input.useGridForLibrary,
            showRecognitionDateInLibrary = input.showRecognitionDateInLibrary
        )
    }
}
