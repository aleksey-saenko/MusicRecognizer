package com.mrsep.musicrecognizer.feature.library.presentation.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun UnviewedTrackBadge(
    modifier: Modifier = Modifier
) {
    Badge(modifier = modifier) {
        Text(
            text = stringResource(StringsR.string.unviewed_track_label),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(2.dp)
        )
    }
}

@Composable
internal fun UnviewedTrackBadgeCustom(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.error,
                shape = shape
            )
            .padding(horizontal = 4.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(StringsR.string.unviewed_track_label),
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onError,
            textAlign = TextAlign.Center
        )
    }
}