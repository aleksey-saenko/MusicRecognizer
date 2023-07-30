package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.mrsep.musicrecognizer.core.strings.R
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask

@Composable
internal fun RecognitionTaskManualMessage(
    recognitionTask: RecognitionTask,
    modifier: Modifier = Modifier
) {
    if (recognitionTask is RecognitionTask.Created) {
        RecognitionTaskMessageBase(
            extraMessage = stringResource(R.string.manual_recognition_message),
            modifier = modifier
        )
    }
}

@Composable
internal fun RecognitionTaskMessageBase(
    extraMessage: String,
    modifier: Modifier = Modifier
) {
    val message = stringResource(R.string.saved_recording_message) +
            if (extraMessage.isNotEmpty()) ", $extraMessage" else ""
    Text(
        text = message,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}