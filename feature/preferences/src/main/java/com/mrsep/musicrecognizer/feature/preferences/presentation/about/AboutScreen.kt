package com.mrsep.musicrecognizer.feature.preferences.presentation.about

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mrsep.musicrecognizer.core.ui.components.VinylRotating
import com.mrsep.musicrecognizer.core.ui.util.openUrlImplicitly
import com.mrsep.musicrecognizer.core.strings.R as StringsR

private const val GITHUB_REPO_URL = "https://github.com/aleksey-saenko/MusicRecognizer"
private const val LICENCE_URL = "https://www.gnu.org/licenses/gpl-3.0.txt"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreen(
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current
    val topBarBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val version = rememberSaveable { context.getVersionName() }

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
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
            VinylRotating(
                modifier = Modifier.size(160.dp)
            )
            Text(
                text = stringResource(StringsR.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = stringResource(StringsR.string.music_recognizing_app),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = stringResource(StringsR.string.ver, version),
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
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(StringsR.string.powered_by_audd),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

private fun Context.getVersionName(): String = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager?.getPackageInfo(
            packageName,
            PackageManager.PackageInfoFlags.of(0)
        )?.versionName
    } else {
        @Suppress("DEPRECATION") packageManager?.getPackageInfo(
            packageName,
            0
        )?.versionName
    } ?: ""
} catch (e: PackageManager.NameNotFoundException) {
    ""
}