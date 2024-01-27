package com.mrsep.musicrecognizer.feature.preferences.presentation.serviceconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.core.strings.R as StringsR

private const val ACR_CLOUD_GUIDE_TAG = "GUIDE_TAG"
private const val ACR_CLOUD_GUIDE_LINK = "https://docs.acrcloud.com/tutorials/recognize-music"

@Composable
internal fun AcrCloudHelpDialog(
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        modifier = modifier,
        title = {
            Text(text = stringResource(StringsR.string.acr_cloud))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val annotatedText = buildAnnotatedString {
                    append(stringResource(StringsR.string.acr_cloud_help_message_start))
                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        pushStringAnnotation(
                            tag = ACR_CLOUD_GUIDE_TAG,
                            annotation = ACR_CLOUD_GUIDE_LINK
                        )
                        append(stringResource(StringsR.string.acr_cloud_help_message_guide_clickable))
                    }
                    append(stringResource(StringsR.string.acr_cloud_help_message_end))
                }
                ClickableText(
                    text = annotatedText,
                    style = LocalTextStyle.current.copy(color = LocalContentColor.current),
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(
                            tag = ACR_CLOUD_GUIDE_TAG,
                            start = offset,
                            end = offset
                        ).firstOrNull()?.item?.let { link ->
                            context.openUrlImplicitly(link)
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(StringsR.string.acr_cloud_help_steps_title),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "\u2022 " + stringResource(StringsR.string.acr_cloud_help_step1))
                    Text(text = "\u2022 " + stringResource(StringsR.string.acr_cloud_help_step2))
                    Text(text = "\u2022 " + stringResource(StringsR.string.acr_cloud_help_step3))
                    Text(text = "\u2022 " + stringResource(StringsR.string.acr_cloud_help_step4))
                }
            }
        },
        onDismissRequest = onDismissClick
    )
}