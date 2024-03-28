package com.mrsep.musicrecognizer.glue.library.mapper

import com.mrsep.musicrecognizer.core.common.BidirectionalMapper
import com.mrsep.musicrecognizer.data.track.TrackDataFieldDo
import com.mrsep.musicrecognizer.feature.library.domain.model.TrackDataField
import javax.inject.Inject

class TrackDataFieldMapper @Inject constructor() : BidirectionalMapper<TrackDataFieldDo, TrackDataField> {

    override fun map(input: TrackDataFieldDo): TrackDataField {
        return when (input) {
            TrackDataFieldDo.Title -> TrackDataField.Title
            TrackDataFieldDo.Artist -> TrackDataField.Artist
            TrackDataFieldDo.Album -> TrackDataField.Album
        }
    }

    override fun reverseMap(input: TrackDataField): TrackDataFieldDo {
        return when (input) {
            TrackDataField.Title -> TrackDataFieldDo.Title
            TrackDataField.Artist -> TrackDataFieldDo.Artist
            TrackDataField.Album -> TrackDataFieldDo.Album
        }
    }
}