package com.mrsep.musicrecognizer.presentation.screens.preferences.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PreferenceSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
//            .background(
//                color = MaterialTheme.colorScheme.primaryContainer,
//                shape = MaterialTheme.shapes.large
//            )
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
//                        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        MaterialTheme.colorScheme.primaryContainer,
                    )
                ),
                shape = MaterialTheme.shapes.large
            )
            .clip(MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        color = Color.Unspecified
    ) {
        content()
    }
}