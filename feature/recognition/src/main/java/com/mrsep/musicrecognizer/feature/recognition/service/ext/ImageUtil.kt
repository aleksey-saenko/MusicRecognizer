package com.mrsep.musicrecognizer.feature.recognition.service.ext

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.Px
import coil3.annotation.ExperimentalCoilApi
import coil3.decode.BlackholeDecoder
import coil3.imageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun Context.getCachedImageOrNull(
    url: String,
    allowHardware: Boolean,
    @Px widthPx: Int? = null,
    @Px heightPx: Int? = null,
): Bitmap? {
    return withContext(Dispatchers.IO) {
        val builder = ImageRequest.Builder(this@getCachedImageOrNull)
            .allowHardware(allowHardware)
            .data(url)
            .networkCachePolicy(CachePolicy.DISABLED)

        if (widthPx != null && heightPx != null) {
            builder.size(widthPx, heightPx)
        }

        val request = builder.build()
        imageLoader.execute(request).image?.toBitmap()
    }
}

@OptIn(ExperimentalCoilApi::class)
internal suspend fun Context.downloadImageToDiskCache(url: String) {
    withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(this@downloadImageToDiskCache)
            .data(url)
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            // Disable reading from/writing to the memory cache
            .memoryCachePolicy(CachePolicy.DISABLED)
            // Skips the decode step so we don't waste time/memory decoding the image into memory
            .decoderFactory(BlackholeDecoder.Factory())
            .build()
        imageLoader.execute(request)
    }
}
