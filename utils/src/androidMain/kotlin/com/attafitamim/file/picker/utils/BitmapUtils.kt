package com.attafitamim.file.picker.utils

import android.graphics.Bitmap
import java.io.OutputStream

fun compressImageToStream(
    source: Bitmap,
    outputStream: OutputStream?,
    quality: Int
) {
    outputStream?.use { stream ->
        source.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    }
}
