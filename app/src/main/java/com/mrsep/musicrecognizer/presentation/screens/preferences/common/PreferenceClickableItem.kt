package com.mrsep.musicrecognizer.presentation.screens.preferences.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreferenceClickableItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onItemClick: () -> Unit
) {
    PreferenceSurface(modifier = modifier.clickable { onItemClick() }) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
            modifier = modifier.padding(16.dp).run {
                if (enabled) this else alpha(0.8f)
            }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 20.sp),
            )
            if (subtitle != null && subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
