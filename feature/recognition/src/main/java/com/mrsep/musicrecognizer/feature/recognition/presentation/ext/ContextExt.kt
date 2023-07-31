package com.mrsep.musicrecognizer.feature.recognition.presentation.ext

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import coil.imageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun Context.fetchBitmapOrNull(url: String): Bitmap? {
    val request = ImageRequest.Builder(this)
        .data(url).allowHardware(false).build()
    return withContext(Dispatchers.IO) {
        when (val result = imageLoader.execute(request)) {
            is SuccessResult -> (result.drawable as BitmapDrawable).bitmap
            is ErrorResult -> null
        }
    }
}