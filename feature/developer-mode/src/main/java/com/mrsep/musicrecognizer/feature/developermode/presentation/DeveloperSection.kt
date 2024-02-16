package com.mrsep.musicrecognizer.feature.developermode.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun DeveloperSection(
    modifier: Modifier = Modifier,
    onRecordClickMR: () -> Unit,
    onStopClickMR: () -> Unit,
    onRecordClickAR: () -> Unit,
    onStopClickAR: () -> Unit,
    onPlayClickMP: () -> Unit,
    onStopClickMP: () -> Unit,
    onRecognizeClick: () -> Unit,
    onFakeRecognizeClick: () -> Unit,
    onClearDatabase: () -> Unit,
    onPrepopulateDatabase: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Surface(
        tonalElevation = if (expanded) 1.dp else 0.dp,
        shadowElevation = if (expanded) 1.dp else 0.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (expanded) {
                    Text(
                        text = "Developer options",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
                Image(
                    painter = painterResource(
                        if (expanded)
                            UiR.drawable.outline_keyboard_arrow_up_24
                        else
                            UiR.drawable.outline_keyboard_arrow_down_24
                    ),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable { expanded = !expanded }
                        .padding(8.dp)
                )
            }
            if (expanded) {
                HorizontalDivider(Modifier.padding(top = 8.dp, bottom = 8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "MediaRecorder", modifier = Modifier.weight(1f))
                    Button(onClick = onRecordClickMR) { Text(text = "Record") }
                    Button(onClick = onStopClickMR) { Text(text = "Stop") }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "AudioRecorder", modifier = Modifier.weight(1f))
                    Button(onClick = onRecordClickAR) { Text(text = "Record") }
                    Button(onClick = onStopClickAR) { Text(text = "Stop") }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "Player", modifier = Modifier.weight(1f))
                    Button(onClick = onPlayClickMP) { Text(text = "Play") }
                    Button(onClick = onStopClickMP) { Text(text = "Stop") }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "Database", modifier = Modifier.weight(1f))
                    Button(onClick = onClearDatabase) { Text(text = "Clear") }
                    Button(onClick = onPrepopulateDatabase) { Text(text = "Load") }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = "Recognize", modifier = Modifier.weight(1f))
                    Button(onClick = onRecognizeClick) { Text(text = "Default") }
                    Button(onClick = onFakeRecognizeClick) { Text(text = "Fake") }
                }
            }

        }
    }

}

