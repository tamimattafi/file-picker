package com.attafitamim.file.picker.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

const val BITMAP_MAX_QUALITY = 100

actual fun ByteArray.toImageBitmap(): ImageBitmap? = runCatching {
    val bitmap = BitmapFactory.decodeByteArray(this, 0, this.size)
    bitmap.asImageBitmap()
}.getOrNull()

fun String.pathToImageBitmap(context: Context, quality: Double = IMAGE_MAX_QUALITY): ImageBitmap? =
    context.contentResolver.openInputStream(
        Uri.parse(this)
    ).use { inputStream ->
        BitmapFactory.decodeStream(inputStream)?.compress(quality)?.asImageBitmap()
    }

fun Bitmap.compress(
    width: Int,
    height: Int,
    quality: Double
): Bitmap {
    if (this.width <= width && this.height <= height) {
        return this
    }

    return compress(quality)
}

fun Bitmap.compress(quality: Double): Bitmap {
    if (quality == IMAGE_MAX_QUALITY) {
        return this
    }

    val bitmapQuality = (BITMAP_MAX_QUALITY * quality).toInt()
    return compress(bitmapQuality)
}

fun Bitmap.compress(quality: Int): Bitmap {
    if (quality == BITMAP_MAX_QUALITY) {
        return this
    }

    val inputBytes = ByteArrayOutputStream().use { outputStream ->
        val format = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            Bitmap.CompressFormat.JPEG
        }

        compress(format, quality, outputStream)
        outputStream.toByteArray()
    }

    return ByteArrayInputStream(inputBytes).use { inputStream ->
        BitmapFactory.decodeStream(inputStream)
    }
}
