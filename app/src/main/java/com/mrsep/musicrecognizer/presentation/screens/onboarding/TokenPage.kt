package com.mrsep.musicrecognizer.presentation.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
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
import com.mrsep.musicrecognizer.R
import com.mrsep.musicrecognizer.util.openUrlImplicitly
import kotlinx.coroutines.launch

private const val SIGN_UP_ANNOTATION_TAG = "SIGN_UP_ANNOTATION_TAG"

class TokenPageState(
    initialTokenInput: String = ""
) {
    var tokenInput by mutableStateOf(initialTokenInput)

    var validationState by mutableStateOf(TokenState.UNCHECKED)

    val isValidating get() = validationState == TokenState.VALIDATING
    val isError get() = validationState == TokenState.ERROR
    val isSuccess get() = validationState == TokenState.SUCCESS
    val isValidationAllowed get() = validationState == TokenState.UNCHECKED || validationState == TokenState.ERROR

    enum class TokenState {
        UNCHECKED, VALIDATING, SUCCESS, ERROR
    }

    companion object {
        val Saver: Saver<TokenPageState, *> = listSaver(
            save = {
                listOf(
                    it.tokenInput,
                    it.validationState.ordinal
                )
            },
            restore = {
                TokenPageState(
                    initialTokenInput = it[0] as String
                ).apply {
                    validationState = TokenState.values()[it[1] as Int]
                }
            }
        )
    }
}

@Composable
fun rememberTokenPageState(
    initialTokenInput: String = ""
): TokenPageState {
    return rememberSaveable(saver = TokenPageState.Saver) {
        TokenPageState(initialTokenInput)
    }
}

@Composable
fun TokenPage(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onTokenApplied: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val state = rememberTokenPageState()

    LaunchedEffect(Unit) {
        val savedToken = viewModel.getSavedToken()
        if (savedToken.isNotBlank()) {
            state.tokenInput = savedToken
        }
    }

    Column(
        modifier = modifier.padding(PaddingValues(24.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = stringResource(R.string.api_token),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(PaddingValues(vertical = 24.dp))
        )

        val annotatedText = buildAnnotatedString {
            append(stringResource(R.string.onboarding_token_message_start))
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                pushStringAnnotation(
                    tag = SIGN_UP_ANNOTATION_TAG,
                    annotation = stringResource(R.string.audd_sign_up_link)
                )
                append(stringResource(R.string.onboarding_token_message_link))
            }
            append(stringResource(R.string.onboarding_token_message_end))
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
            value = state.tokenInput,
            onValueChange = {
                state.tokenInput = it
                state.validationState = TokenPageState.TokenState.UNCHECKED
            },
            label = { Text(stringResource(R.string.audd_api_token)) },
            singleLine = true,
            isError = state.isError,
            supportingText = {
                AnimatedVisibility(state.isError) {
                    Text(stringResource(R.string.invalid_token))
                }
            },
            trailingIcon = if (state.isError) {
                {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_error_filled_24),
                        contentDescription = stringResource(R.string.invalid_token)
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
            onClick = {
                scope.launch {
                    state.validationState = TokenPageState.TokenState.VALIDATING
                    val isTokenValid = viewModel.validateAndSaveToken(state.tokenInput)
                    if (isTokenValid) {
                        state.validationState = TokenPageState.TokenState.SUCCESS
                        onTokenApplied()
                    } else {
                        state.validationState = TokenPageState.TokenState.ERROR
                    }
                }
            },
            enabled = state.isValidationAllowed
        ) {
            Text(
                text = if (state.isSuccess) {
                    stringResource(R.string.token_applied)
                } else if (state.isValidating) {
                    "Validating"
                } else {
                    stringResource(R.string.apply_api_token)
                }
            )
        }

        Row(modifier = Modifier.height(4.dp)) {
            AnimatedVisibility(state.isValidating) {
                LinearProgressIndicator(modifier = Modifier.clip(MaterialTheme.shapes.extraSmall))
            }
        }
    }

}