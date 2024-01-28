package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.core.ui.components.VinylRotating
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.ConfigValidationStatus
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

private const val SIGN_UP_ANNOTATION_TAG = "SIGN_UP_TAG"

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TokenPage(
    modifier: Modifier = Modifier,
    uiState: TokenPageUiState,
    onTokenChanged: (String) -> Unit,
    onTokenSkip: () -> Unit,
    onTokenValidate: () -> Unit,
    onTokenApplied: () -> Unit
) {
    val context = LocalContext.current
    when (uiState) {
        TokenPageUiState.Loading -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            LoadingStub(modifier = Modifier.weight(weight = 1f))
        }

        is TokenPageUiState.Success -> {
            LaunchedEffect(uiState) {
                if (uiState.configValidationStatus is ConfigValidationStatus.Success) onTokenApplied()
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .fillMaxSize()
                    .padding(PaddingValues(24.dp)),
            ) {

                Text(
                    text = stringResource(StringsR.string.api_token),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 24.dp)
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
                        .padding(top = 24.dp)
                )
                Text(
                    text = stringResource(StringsR.string.token_skip_message),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(max = 488.dp)
                        .padding(top = 4.dp)
                )

                val errorMessage = uiState.configValidationStatus.errorMessageOrNull()
                var passwordVisible by rememberSaveable { mutableStateOf(false) }
                OutlinedTextField(
                    modifier = Modifier
                        .widthIn(min = 56.dp, max = 488.dp)
                        .fillMaxWidth(0.95f)
                        .padding(top = 24.dp),
                    value = uiState.token,
                    onValueChange = onTokenChanged,
                    readOnly = uiState.configValidationStatus is ConfigValidationStatus.Validating,
                    label = { Text(stringResource(StringsR.string.api_token)) },
                    trailingIcon = {
                        val iconPainter = painterResource(
                            if (passwordVisible) {
                                UiR.drawable.baseline_visibility_off_24
                            } else {
                                UiR.drawable.baseline_visibility_24
                            }
                        )
                        val iconDesc = stringResource(
                            if (passwordVisible) StringsR.string.hide else StringsR.string.show
                        )
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                painter = iconPainter,
                                contentDescription = iconDesc
                            )
                        }
                    },
                    supportingText = {
                        Text(errorMessage ?: "")
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    isError = errorMessage != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    shape = MaterialTheme.shapes.small
                )

                FlowRow(
                    modifier = Modifier.padding(top = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onTokenSkip,
                        enabled = uiState.configValidationStatus.isValidationAllowed,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = stringResource(StringsR.string.skip)
                        )
                    }
                    Button(
                        onClick = onTokenValidate,
                        enabled = uiState.configValidationStatus.isValidationAllowed,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        when (uiState.configValidationStatus) {
                            ConfigValidationStatus.Success -> Text(
                                text = stringResource(StringsR.string.applied)
                            )

                            ConfigValidationStatus.Validating -> Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(text = stringResource(StringsR.string.validating))
                                VinylRotating(modifier = Modifier.size(24.dp))
                            }

                            else -> Text(text = stringResource(StringsR.string.apply))
                        }
                    }
                }
            }
        }
    }


}

@Stable
@Composable
private fun ConfigValidationStatus.errorMessageOrNull() = when (this) {
    ConfigValidationStatus.Error.Empty -> stringResource(StringsR.string.must_not_be_empty)
    ConfigValidationStatus.Error.AuthError -> stringResource(StringsR.string.auth_error)
    ConfigValidationStatus.Error.ApiUsageLimited -> stringResource(StringsR.string.service_usage_limited)
    ConfigValidationStatus.Error.BadConnection -> stringResource(StringsR.string.bad_internet_connection)
    ConfigValidationStatus.Error.UnknownError -> stringResource(StringsR.string.unknown_error)
    ConfigValidationStatus.Unchecked,
    ConfigValidationStatus.Validating,
    ConfigValidationStatus.Success -> null
}