package com.mrsep.musicrecognizer.feature.track.presentation.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun AutoScrollPanel(
    modifier: Modifier = Modifier,
    scrollSpeed: Float,
    onScrollSpeedChange: (Float) -> Unit,
    onStopScrollClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                shape = CircleShape
            )
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Slider(
            value = scrollSpeed,
            onValueChange = onScrollSpeedChange,
            valueRange = 0.5f..2f,
            modifier = Modifier
                .height(156.dp)
                .width(56.dp)
                .graphicsLayer {
                    rotationZ = 270f
                    transformOrigin = TransformOrigin(0f, 0f)
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        Constraints(
                            minWidth = constraints.minHeight,
                            maxWidth = constraints.maxHeight,
                            minHeight = constraints.minWidth,
                            maxHeight = constraints.maxHeight,
                        )
                    )
                    layout(placeable.height, placeable.width) {
                        placeable.place(-placeable.width, 0)
                    }
                }
        )
        Spacer(modifier = Modifier.height(12.dp))
        FilledIconButton(onClick = onStopScrollClick) {
            Icon(
                painter = painterResource(UiR.drawable.outline_pause_fill1_24),
                contentDescription = stringResource(StringsR.string.stop_autoscroll)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}