package com.mrsep.musicrecognizer.feature.track.presentation.track

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
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
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmapOrNull
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.mrsep.musicrecognizer.core.ui.util.forwardingPainter
import com.mrsep.musicrecognizer.feature.track.presentation.utils.getDominantColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mrsep.musicrecognizer.core.ui.R as UiR
import com.mrsep.musicrecognizer.core.strings.R as StringsR

@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun AlbumArtwork(
    url: String?,
    onArtworkCached: (Uri) -> Unit,
    createSeedColor: Boolean,
    onSeedColorCreated: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    val placeholder = forwardingPainter(
        painter = painterResource(UiR.drawable.baseline_album_24),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
        alpha = 0.3f
    )
    val context = LocalContext.current
    val imageLoader = LocalContext.current.imageLoader
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
                    state.result.drawable.toBitmapOrNull()?.getDominantColor()?.let { seedColor ->
                        withContext(Dispatchers.Main) {
                            onSeedColorCreated(seedColor)
                        }
                    }
                }
            }
            scope.launch(Dispatchers.IO) {
                state.result.diskCacheKey?.let { diskCacheKey ->
                    imageLoader.diskCache?.openSnapshot(diskCacheKey)?.let { snapshot ->
                        val cacheUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            snapshot.data.toFile()
                        )
                        snapshot.close()
                        withContext(Dispatchers.Main) {
                            onArtworkCached(cacheUri)
                        }
                    }
                }
            }
        }
    )
    Image(
        painter = painter,
        contentDescription = stringResource(StringsR.string.artwork),
        contentScale = ContentScale.Crop,
        modifier = modifier
            .shadow(
                elevation = 1.dp,
                shape = MaterialTheme.shapes.extraLarge
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                shape = MaterialTheme.shapes.extraLarge
            )
    )
}