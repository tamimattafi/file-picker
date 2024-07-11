package com.attafitamim.file.picker.utils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun ByteArray.toImageBitmap(): ImageBitmap? = runCatching {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
    bitmap.asImageBitmap()
}.getOrNull()
