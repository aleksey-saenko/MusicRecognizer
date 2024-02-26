package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.common.util.getAppVersion
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceClickableItem
import com.mrsep.musicrecognizer.feature.preferences.presentation.common.PreferenceGroup
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

private const val AUDD_URL = "https://audd.io/"
private const val ACR_CLOUD_URL = "https://www.acrcloud.com/"
private const val ODESLI_URL = "https://odesli.co/"
private const val GITHUB_REPO_URL = "https://github.com/aleksey-saenko/MusicRecognizer.git"
private const val LICENCE_URL = "https://www.gnu.org/licenses/gpl-3.0.txt"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreen(
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current
    val topBarBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val version = rememberSaveable { context.getAppVersion() }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        AboutScreenTopBar(
            onBackPressed = onBackPressed,
            scrollBehavior = topBarBehavior
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(topBarBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(4.dp))
            AppLogo(
                modifier = Modifier.size(120.dp)
            )
            Text(
                text = stringResource(StringsR.string.app_name),
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = version,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(Modifier.height(16.dp))
            PreferenceGroup(title = stringResource(StringsR.string.powered_by)) {
                PreferenceClickableItem(
                    title = stringResource(StringsR.string.audd),
                    subtitle = stringResource(StringsR.string.purpose_recognition_service),
                    onItemClick = { context.openUrlImplicitly(AUDD_URL) }
                )
                PreferenceClickableItem(
                    title = stringResource(StringsR.string.acr_cloud),
                    subtitle = stringResource(StringsR.string.purpose_recognition_service),
                    onItemClick = { context.openUrlImplicitly(ACR_CLOUD_URL) }
                )
                PreferenceClickableItem(
                    title = stringResource(StringsR.string.odesli),
                    subtitle = stringResource(StringsR.string.purpose_track_links_service),
                    onItemClick = { context.openUrlImplicitly(ODESLI_URL) }
                )
            }
            Spacer(Modifier.height(16.dp))
            PreferenceGroup(title = stringResource(StringsR.string.misc)) {
                PreferenceClickableItem(
                    title = stringResource(StringsR.string.github_repository),
                    onItemClick = { context.openUrlImplicitly(GITHUB_REPO_URL) }
                )
                PreferenceClickableItem(
                    title = stringResource(StringsR.string.license_gnu_gplv3),
                    onItemClick = { context.openUrlImplicitly(LICENCE_URL) }
                )
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