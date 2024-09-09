package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.ConfigValidationStatus
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun TokenPage(
    modifier: Modifier = Modifier,
    uiState: TokenPageUiState,
    onTokenChanged: (String) -> Unit,
    onTokenValidate: () -> Unit,
    onTokenApplied: () -> Unit
) {
    when (uiState) {
        TokenPageUiState.Loading -> LoadingStub(
            modifier = modifier
                .background(color = MaterialTheme.colorScheme.surface)
        )

        is TokenPageUiState.Success -> {
            var isTokenApplied by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(uiState.configValidationStatus) {
                when (uiState.configValidationStatus) {
                    ConfigValidationStatus.Success -> if (!isTokenApplied) {
                        isTokenApplied = true
                        onTokenApplied()
                    }
                    else -> isTokenApplied = false
                }
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
            ) {
                Text(
                    text = stringResource(StringsR.string.api_token),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = AnnotatedString.fromHtml(
                        htmlString = stringResource(
                            StringsR.string.onboarding_token_message,
                            stringResource(StringsR.string.audd_sign_up_url)
                        ),
                        linkStyles = TextLinkStyles(
                            style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                            hoveredStyle = SpanStyle(textDecoration = TextDecoration.Underline),
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 488.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(StringsR.string.token_skip_message),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 488.dp)
                )

                Spacer(Modifier.height(24.dp))
                val errorMessage = uiState.configValidationStatus.errorMessageOrNull()
                var passwordVisible by rememberSaveable { mutableStateOf(false) }
                OutlinedTextField(
                    modifier = Modifier
                        .widthIn(min = 56.dp, max = 488.dp)
                        .fillMaxWidth(0.95f),
                    value = uiState.token,
                    onValueChange = onTokenChanged,
                    readOnly = uiState.configValidationStatus is ConfigValidationStatus.Validating,
                    label = { Text(stringResource(StringsR.string.api_token)) },
                    trailingIcon = {
                        val iconPainter = painterResource(
                            if (passwordVisible) {
                                UiR.drawable.outline_visibility_off_24
                            } else {
                                UiR.drawable.outline_visibility_24
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

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onTokenValidate,
                    enabled = uiState.configValidationStatus.isValidationAllowed,
                    modifier = Modifier.widthIn(min = 240.dp)
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
                            VinylRotating(modifier = Modifier.size(20.dp))
                        }

                        else -> Text(text = stringResource(StringsR.string.apply))
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
