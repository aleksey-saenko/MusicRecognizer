package com.mrsep.musicrecognizer.feature.track.presentation.track

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import coil3.size.Size
import coil3.toBitmap
import com.mrsep.musicrecognizer.core.ui.util.forwardingPainter
import com.mrsep.musicrecognizer.feature.track.presentation.utils.getDominantColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mrsep.musicrecognizer.core.strings.R as StringsR
import com.mrsep.musicrecognizer.core.ui.R as UiR

@Composable
internal fun AlbumArtwork(
    url: String?,
    onArtworkClick: () -> Unit,
    createSeedColor: Boolean,
    onSeedColorCreated: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeholder = forwardingPainter(
        painter = painterResource(UiR.drawable.outline_album_fill1_24),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
        alpha = 0.3f
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(url)
            .size(Size.ORIGINAL)
            .allowHardware(!createSeedColor)
            .crossfade(50)
            .build(),
        error = placeholder,
        fallback = placeholder,
        contentScale = ContentScale.Crop,
        onSuccess = { state ->
            if (!state.result.request.allowHardware) {
                scope.launch(Dispatchers.Default) {
                    val seedColor = state.result.image.toBitmap().getDominantColor() ?: return@launch
                    withContext(Dispatchers.Main) { onSeedColorCreated(seedColor) }
                }
            }
        }
    )
    val painterState by painter.state.collectAsStateWithLifecycle()
    Image(
        painter = painter,
        contentDescription = stringResource(StringsR.string.artwork),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = MaterialTheme.shapes.extraLarge
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.extraLarge
            )
            .clickable(
                enabled = painterState is AsyncImagePainter.State.Success,
                onClick = onArtworkClick
            )
    )
}
