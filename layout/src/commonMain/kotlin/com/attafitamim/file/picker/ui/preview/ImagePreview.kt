package com.attafitamim.file.picker.ui.preview

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.size.Precision
import com.attafitamim.file.picker.utils.rememberImageRequestWithHEICSupport

internal const val DefaultAspectRatio = 1f
internal const val DefaultQuality = 0.5

@Composable
fun ImagePreview(
    uri: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    aspectRatio: Float = DefaultAspectRatio,
    quality: Double = DefaultQuality,
    placeholder: Painter = ColorPainter(Color.LightGray),
    error: Painter = ColorPainter(Color.DarkGray),
    preciseResult: Boolean = false,
    enableCache: Boolean = true,
    contentDescription: String? = null
) {
    val precision = remember(preciseResult) {
        if (preciseResult) {
            Precision.EXACT
        } else {
            Precision.INEXACT
        }
    }

    val cachePolicy = remember(enableCache) {
        if (enableCache) {
            CachePolicy.ENABLED
        } else {
            CachePolicy.DISABLED
        }
    }

    AsyncImage(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .then(modifier),
        model = rememberImageRequestWithHEICSupport(
            uri = uri,
            compressionQuality = quality
        ).precision(precision)
            .memoryCachePolicy(cachePolicy)
            .networkCachePolicy(cachePolicy)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        placeholder = placeholder,
        error = error
    )
}

