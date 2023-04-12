package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

private const val SIGN_UP_ANNOTATION_TAG = "SIGN_UP_ANNOTATION_TAG"

@Composable
internal fun TokenPage(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onTokenApplied: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var tokenInput by rememberSaveable { mutableStateOf("") }
    val tokenState by viewModel.tokenState.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle(null)
    //FIXME wait for preferences loading (show stub)
    LaunchedEffect(preferences) {
        preferences?.apiToken?.let { token ->
            if (token.isNotBlank()) {
                tokenInput = token
            }
        }
    }
    if (tokenState is TokenState.Success) {
        LaunchedEffect(Unit) { onTokenApplied() }
    }

    Column(
        modifier = modifier.padding(PaddingValues(24.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(StringsR.string.api_token),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(PaddingValues(vertical = 24.dp))
        )

        val annotatedText = buildAnnotatedString {
            append(stringResource(StringsR.string.onboarding_token_message_start))
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                pushStringAnnotation(
                    tag = SIGN_UP_ANNOTATION_TAG,
                    annotation = stringResource(StringsR.string.audd_sign_up_link)
                )
                append(stringResource(StringsR.string.onboarding_token_message_link))
            }
            append(stringResource(StringsR.string.onboarding_token_message_end))
        }

        ClickableText(
            text = annotatedText,
            style = MaterialTheme.typography.bodyLarge.copy(
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            ),
            onClick = { offset ->
                annotatedText.getStringAnnotations(
                    tag = SIGN_UP_ANNOTATION_TAG,
                    start = offset,
                    end = offset
                ).firstOrNull()?.item?.let { link ->
                    context.openUrlImplicitly(link)
                }
            },
            modifier = Modifier
                .widthIn(max = 488.dp)
                .padding(bottom = 24.dp)
        )

        OutlinedTextField(
            modifier = Modifier
                .widthIn(min = 56.dp, max = 488.dp)
                .fillMaxWidth(0.9f)
                .padding(bottom = 24.dp),
            value = tokenInput,
            onValueChange = {
                tokenInput = it
                viewModel.resetTokenState()
            },
            label = { Text(stringResource(StringsR.string.audd_api_token)) },
            singleLine = true,
            isError = tokenState.isBadToken,
            supportingText = {
                AnimatedVisibility(tokenState.isBadToken) {
                    Text(stringResource(StringsR.string.invalid_token))
                }
            },
            trailingIcon = if (tokenState.isBadToken) {
                {
                    Icon(
                        imageVector = ImageVector.vectorResource(UiR.drawable.ic_error_filled_24),
                        contentDescription = stringResource(StringsR.string.invalid_token)
                    )
                }
            } else {
                null
            }
        )
        Button(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .widthIn(min = 240.dp),
            onClick = { viewModel.testToken(tokenInput) },
            enabled = tokenState.isValidationAllowed
        ) {
            Text(
                text = if (tokenState.isSuccessToken) {
                    stringResource(StringsR.string.token_applied)
                } else if (tokenState.isValidating) {
                    "Validating"
                } else {
                    stringResource(StringsR.string.apply_api_token)
                }
            )
        }

        Row(modifier = Modifier.height(4.dp)) {
            AnimatedVisibility(tokenState.isValidating) {
                LinearProgressIndicator(modifier = Modifier.clip(MaterialTheme.shapes.extraSmall))
            }
        }
    }

}


private fun TokenState.isValidating() = this is TokenState.Validating
private fun TokenState.isBadToken() = this is TokenState.Wrong
private fun TokenState.isSuccessToken() = this is TokenState.Success
private fun TokenState.isValidationAllowed() = this !is TokenState.Validating && this !is TokenState.Success