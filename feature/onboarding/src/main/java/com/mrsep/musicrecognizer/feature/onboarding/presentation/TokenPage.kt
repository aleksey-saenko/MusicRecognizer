package com.mrsep.musicrecognizer.feature.onboarding.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
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
import com.mrsep.musicrecognizer.core.ui.components.LoadingStub
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.feature.onboarding.domain.model.TokenValidationStatus
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

private const val SIGN_UP_ANNOTATION_TAG = "SIGN_UP_ANNOTATION_TAG"

@Composable
internal fun TokenPage(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onTokenApplied: () -> Unit
) {
    val context = LocalContext.current
    val pageUiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val uiState = pageUiState) {
        TokenPageUiState.Loading -> Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize()
        ) {
            LoadingStub(
                modifier = Modifier.weight(weight = 1f)
            )
        }

        is TokenPageUiState.Success -> {
            LaunchedEffect(uiState) {
                if (uiState.tokenValidationStatus is TokenValidationStatus.Success) onTokenApplied()
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

                val errorMessage = uiState.tokenValidationStatus.errorMessageOrNull()
                OutlinedTextField(
                    modifier = Modifier
                        .widthIn(min = 56.dp, max = 488.dp)
                        .fillMaxWidth(0.9f)
                        .padding(bottom = 24.dp),
                    value = uiState.token,
                    readOnly = uiState.tokenValidationStatus is TokenValidationStatus.Validating,
                    onValueChange = viewModel::setTokenField,
                    label = { Text(stringResource(StringsR.string.audd_api_token)) },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        Text(errorMessage ?: "")
                    },
                    trailingIcon = errorMessage?.let {
                        {
                            Icon(
                                imageVector = ImageVector.vectorResource(UiR.drawable.ic_error_filled_24),
                                contentDescription = errorMessage
                            )
                        }
                    }
                )
                Button(
                    modifier = Modifier.widthIn(min = 240.dp),
                    onClick = viewModel::testToken,
                    enabled = uiState.tokenValidationStatus.isValidationAllowed
                ) {
                    Text(
                        text = when (uiState.tokenValidationStatus) {
                            TokenValidationStatus.Success -> stringResource(StringsR.string.token_applied)
                            TokenValidationStatus.Validating -> stringResource(StringsR.string.validating)
                            else -> stringResource(StringsR.string.apply_api_token)
                        }
                    )
                }

                Box(modifier = Modifier.padding(top = 24.dp).height(4.dp)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.tokenValidationStatus is TokenValidationStatus.Validating
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.height(4.dp),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }

                Button(
                    modifier = Modifier.widthIn(min = 240.dp),
                    onClick = onTokenApplied,
                    enabled = uiState.tokenValidationStatus.isValidationAllowed
                ) {
                    Text(
                        text = when (uiState.tokenValidationStatus) {
                            TokenValidationStatus.Success -> stringResource(StringsR.string.token_applied)
                            TokenValidationStatus.Validating -> stringResource(StringsR.string.validating)
                            else -> stringResource(StringsR.string.apply_api_token)
                        }
                    )
                }
            }
        }
    }


}

@Stable
@Composable
private fun TokenValidationStatus.errorMessageOrNull() = when (this) {
    TokenValidationStatus.Error.BadConnection -> stringResource(StringsR.string.bad_internet_connection)
    TokenValidationStatus.Error.UnknownError -> stringResource(StringsR.string.unknown_error)
    is TokenValidationStatus.Error.WrongToken -> if (isLimitReached)
        stringResource(StringsR.string.token_limit_reached)
    else
        stringResource(StringsR.string.wrong_token)
    else -> null
}