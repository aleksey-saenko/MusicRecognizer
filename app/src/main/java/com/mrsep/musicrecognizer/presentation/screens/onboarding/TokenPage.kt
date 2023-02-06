package com.mrsep.musicrecognizer.presentation.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mrsep.musicrecognizer.R

private const val SIGN_UP_ANNOTATION_TAG = "SIGN_UP_ANNOTATION_TAG"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenPage(
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
    onSignUpClick: (String) -> Unit,
    onApplyTokenClick: () -> Unit
) {
    var progressBarEnable by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.api_token),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
        )

        val annotatedText = buildAnnotatedString {
            append(stringResource(R.string.token_requirement_start))
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
                append(stringResource(R.string.token_requirement_link))
            }
            append(stringResource(R.string.token_requirement_end))
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
                    onSignUpClick(link)
                }
            },
            modifier = Modifier
                .widthIn(max = 488.dp)
                .padding(top = 8.dp, bottom = 24.dp)
        )
        OutlinedTextField(
            modifier = Modifier
                .widthIn(min = 56.dp, max = 488.dp)
                .fillMaxWidth(0.9f)
                .padding(bottom = 24.dp),
            value = viewModel.apiToken,
            onValueChange = { viewModel.updateApiToken(it) },
            label = { Text(stringResource(R.string.audd_api_token)) },
            singleLine = true,
        )

        Button(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .widthIn(min = 240.dp),
            onClick = {
                progressBarEnable = !progressBarEnable
                viewModel.applyToken()
                onApplyTokenClick()
            }
        ) {
            Text(text = stringResource(R.string.apply_api_token))
        }
        Box(modifier = Modifier.height(4.dp)) {
            androidx.compose.animation.AnimatedVisibility(progressBarEnable) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                )
            }
        }


    }
}