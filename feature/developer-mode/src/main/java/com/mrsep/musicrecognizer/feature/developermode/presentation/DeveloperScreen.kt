package com.mrsep.musicrecognizer.feature.developermode.presentation

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrsep.musicrecognizer.core.strings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeveloperScreen(
    onBackPressed: () -> Unit,
    viewModel: DeveloperViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val topBarBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(isProcessing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )
        }
        DeveloperScreenTopBar(
            topAppBarScrollBehavior = topBarBehaviour,
            onBackPressed = onBackPressed
        )
        Column(
            modifier = Modifier
                .nestedScroll(topBarBehaviour.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ButtonGroup(
                title = "DATABASE",
                content = {
                    Button(onClick = viewModel::clearDb) { Text(text = "Clear") }
                    Button(onClick = viewModel::prepopulateDbFakes) { Text(text = "Load fake") }
                    Button(onClick = viewModel::prepopulateDbAssets) { Text(text = "Load real") }
                }
            )
            ButtonGroup(
                title = "RECOGNITION",
                content = {
                    Button(onClick = { showStubToast(context) }) { Text(text = "Fake impl") }
                    Button(onClick = { showStubToast(context) }) { Text(text = "Real impl") }
                }
            )

        }
    }


}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ButtonGroup(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.large,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }

}

private fun showStubToast(context: Context) {
    Toast.makeText(context, context.getString(R.string.not_implemented), Toast.LENGTH_LONG).show()
}