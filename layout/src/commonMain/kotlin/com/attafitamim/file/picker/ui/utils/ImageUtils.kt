package com.attafitamim.file.picker.ui.utils

import androidx.compose.ui.graphics.ImageBitmap

const val IMAGE_MAX_QUALITY = 1.0

expect fun ByteArray.toImageBitmap(): ImageBitmap?
