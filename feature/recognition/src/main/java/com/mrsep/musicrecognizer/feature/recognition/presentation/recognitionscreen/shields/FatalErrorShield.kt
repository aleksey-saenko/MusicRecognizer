package com.mrsep.musicrecognizer.feature.recognition.presentation.recognitionscreen.shields

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.util.copyTextToClipboard
import com.mrsep.musicrecognizer.core.ui.util.shareText
import com.mrsep.musicrecognizer.feature.recognition.domain.model.RecognitionTask
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun AnimatedVisibilityScope.FatalErrorShield(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
    moreInfo: String,
    recognitionTask: RecognitionTask,
    onDismissClick: () -> Unit,
    onRetryClick: () -> Unit,
    onNavigateToQueue: (recognitionId: Int?) -> Unit
) {
    val context = LocalContext.current
    BaseShield(
        modifier = modifier,
        onDismissClick = onDismissClick
    ) {
        Icon(
            painter = painterResource(UiR.drawable.outline_error_24),
            modifier = Modifier.size(64.dp),
            contentDescription = null
        )
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = message,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        RecognitionTaskManualMessage(
            recognitionTask = recognitionTask,
            modifier = Modifier.padding(top = 16.dp)
        )
        var causeExpanded by rememberSaveable { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            if (causeExpanded) {
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = moreInfo,
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                        )
                        Row(modifier = Modifier.padding(top = 16.dp)) {
                            TextButton(
                                onClick = { context.copyTextToClipboard(moreInfo) },
                            ) {
                                Icon(
                                    painter = painterResource(UiR.drawable.outline_content_copy_24),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(StringsR.string.copy))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            TextButton(
                                onClick = { context.shareText(subject = "", body = moreInfo) },
                            ) {
                                Icon(
                                    painter = painterResource(UiR.drawable.outline_share_24),
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = stringResource(StringsR.string.share))
                            }
                        }
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { causeExpanded = !causeExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (causeExpanded)
                        stringResource(StringsR.string.show_less)
                    else
                        stringResource(StringsR.string.show_more)
                )
            }

            if (recognitionTask is RecognitionTask.Created) {
                FilledTonalButton(
                    onClick = { onNavigateToQueue(recognitionTask.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(StringsR.string.recognition_queue))
                }
            }

            FilledTonalButton(
                onClick = onRetryClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(StringsR.string.retry))
            }
        }
    }
}