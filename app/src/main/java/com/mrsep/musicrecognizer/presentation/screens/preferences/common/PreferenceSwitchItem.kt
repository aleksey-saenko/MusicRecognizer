package com.mrsep.musicrecognizer.presentation.screens.preferences.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PreferenceSwitchItem(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    checked: Boolean
) {
    PreferenceSurface(
        modifier = modifier
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f).run {
                    if (enabled) this else alpha(0.8f)
                }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 20.sp)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = { onCheckedChange(it) }
            )
        }
    }
}