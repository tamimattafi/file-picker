package com.attafitamim.file.picker.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize

@Composable
actual fun MediaState.asThumbnailBitmapState(
    contentScale: ContentScale,
    size: IntSize?,
    quality: Double
): State<ImageBitmap?> {
    TODO("Not yet implemented")
}