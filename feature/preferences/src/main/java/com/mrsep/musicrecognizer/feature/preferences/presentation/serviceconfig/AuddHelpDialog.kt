package com.mrsep.musicrecognizer.feature.preferences.presentation.serviceconfig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@Composable
internal fun AuddHelpDialog(
    modifier: Modifier = Modifier,
    onDismissClick: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        title = {
            Text(text = stringResource(StringsR.string.audd))
        },
        confirmButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(StringsR.string.close))
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                val annotatedText = buildAnnotatedString {
                    append(stringResource(StringsR.string.audd_help_message_start))
                    withLink(
                        LinkAnnotation.Url(
                            url = stringResource(StringsR.string.audd_sign_up_url),
                            styles = TextLinkStyles(
                                style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                                hoveredStyle = SpanStyle(textDecoration = TextDecoration.Underline),
                            )
                        )
                    ) {
                        append(stringResource(StringsR.string.audd_help_message_clickable))
                    }
                    append(stringResource(StringsR.string.audd_help_message_end))
                }
                Text(text = annotatedText)
                Spacer(Modifier.height(16.dp))
                Text(text = stringResource(StringsR.string.audd_help_message_empty_token))
            }
        },
        onDismissRequest = onDismissClick
    )
}
