package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun AnimatedVisibilityScope.ApiUsageLimitedShield(
    modifier: Modifier = Modifier,
    recognitionTask: RecognitionTask,
    onDismissClick: () -> Unit,
    onNavigateToQueue: (recognitionId: Int?) -> Unit,
    onNavigateToPreferences: () -> Unit
) {
    BaseShield(
        modifier = modifier,
        onDismissClick = onDismissClick
    ) {
        Icon(
            painter = painterResource(UiR.drawable.outline_speed_24),
            modifier = Modifier.size(64.dp),
            contentDescription = null
        )
        Text(
            text = stringResource(StringsR.string.service_usage_limited),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(StringsR.string.service_usage_limited_message),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        RecognitionTaskManualMessage(
            recognitionTask = recognitionTask,
            modifier = Modifier.padding(top = 16.dp)
        )
        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (recognitionTask is RecognitionTask.Created) {
                FilledTonalButton(
                    onClick = { onNavigateToQueue(recognitionTask.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(StringsR.string.recognition_queue))
                }
            }
            FilledTonalButton(
                onClick = onNavigateToPreferences,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(StringsR.string.preferences))
            }
        }
    }
}
