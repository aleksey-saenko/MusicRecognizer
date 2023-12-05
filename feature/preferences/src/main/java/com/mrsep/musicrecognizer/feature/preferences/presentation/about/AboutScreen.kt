package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.common.util.getAppVersion
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

private const val GITHUB_REPO_URL = "https://github.com/aleksey-saenko/MusicRecognizer"
private const val LICENCE_URL = "https://www.gnu.org/licenses/gpl-3.0.txt"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreen(
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current
    val topBarBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val version = rememberSaveable { context.getAppVersion() }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        AboutScreenTopBar(
            onBackPressed = onBackPressed,
            topAppBarScrollBehavior = topBarBehavior
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AppLogo(
                modifier = Modifier.size(120.dp)
            )
            Text(
                text = stringResource(StringsR.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = stringResource(StringsR.string.format_version, version),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(StringsR.string.music_recognizing_app),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(StringsR.string.powered_by_audd),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Column(
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                FilledTonalButton(
                    onClick = { context.openUrlImplicitly(LICENCE_URL) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Text(text = stringResource(StringsR.string.license_gnu_gplv3))
                }
                FilledTonalButton(
                    onClick = { context.openUrlImplicitly(GITHUB_REPO_URL) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(text = stringResource(StringsR.string.github_repository))
                }
            }
        }
    }
}

@Composable
private fun AppLogo(
    modifier: Modifier = Modifier
) {
    val iconBrush = Brush.verticalGradient(
        0.2f to MaterialTheme.colorScheme.onSecondaryContainer,
        1f to MaterialTheme.colorScheme.primary
    )
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(UiR.drawable.ic_retro_microphone),
            contentDescription = null,
            modifier = Modifier
                .size(84.dp)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(
                            brush = iconBrush,
                            blendMode = BlendMode.SrcAtop
                        )
                    }
                }
        )
    }
}