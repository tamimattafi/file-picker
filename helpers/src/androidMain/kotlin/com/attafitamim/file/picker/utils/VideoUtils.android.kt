@file:OptIn(ExperimentalCoilApi::class)

package com.attafitamim.file.picker.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.video.VideoFrameDecoder
import coil3.video.videoFrameMillis
import com.attafitamim.file.picker.core.utils.SECOND_IN_MILLIS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun rememberImageFromVideo(
    url: String,
    time: Double,
    isUrl: Boolean,
    quality: Double
): ImageBitmap? {
    var bitmap: ImageBitmap? by remember(url, time) { mutableStateOf(null) }
    val platformContext = LocalContext.current
    val loader = remember {
        ImageLoader.Builder(platformContext)
            .components {
                add(VideoFrameDecoder.Factory())
            }.build()
    }

    LaunchedEffect(url, time) {
        val videoMillis = (SECOND_IN_MILLIS * time).toLong()
        val loadedBitmap = withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(platformContext)
                .decoderFactory(VideoFrameDecoder.Factory())
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .videoFrameMillis(videoMillis)
                .data(url)
                .build()

            loader.execute(request).image?.compress(quality)
        }

        bitmap = loadedBitmap?.asImageBitmap()
    }

    return bitmap
}
