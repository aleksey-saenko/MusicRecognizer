package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import com.mrsep.musicrecognizer.core.common.util.getAppVersion

@Composable
internal fun DebugBuildLabel(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appVersion = "Version ${context.getAppVersion()}"
    Text(
        text = "$appVersion\nDevelopment build",
        modifier = modifier.alpha(0.8f),
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace
        )
    )
}