package com.attafitamim.file.picker.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

@Composable
expect fun rememberImageFromVideo(
    url: String,
    time: Double,
    isUrl: Boolean,
    quality: Double
): ImageBitmap?