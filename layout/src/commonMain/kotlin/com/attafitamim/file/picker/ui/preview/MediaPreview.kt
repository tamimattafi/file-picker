package com.attafitamim.file.picker.ui.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize

const val DefaultThumbnailQuality = 1.0

@Composable
expect fun MediaState.asThumbnailBitmapState(
    contentScale: ContentScale,
    size: IntSize?,
    quality: Double
): State<ImageBitmap?>

@Composable
fun MediaThumbnail(
    modifier: Modifier = Modifier,
    state: MediaState,
    contentScale: ContentScale = ContentScale.Fit,
    quality: Double = DefaultThumbnailQuality,
    contentDescription: String? = null,

) {
    var thumbnailSize: IntSize? by remember {
        mutableStateOf(null)
    }

    val bitmap by state.asThumbnailBitmapState(contentScale, thumbnailSize, quality)
    Box(
        modifier = modifier.onSizeChanged { newSize ->
            if (newSize.width != 0 && newSize.height != 0) {
                thumbnailSize = newSize
            }
        }
    ) {
        bitmap?.let { imageBitmap ->
            Image(
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                bitmap = imageBitmap,
                contentDescription = contentDescription,
            )
        }
    }
}