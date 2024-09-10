package com.attafitamim.file.picker.ui.preview

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import android.provider.MediaStore
import android.util.Size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import com.attafitamim.file.picker.ui.preview.MediaType.IMAGE
import com.attafitamim.file.picker.ui.preview.MediaType.VIDEO
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

@Composable
actual fun MediaState.asThumbnailBitmapState(
    contentScale: ContentScale,
    size: IntSize?,
    quality: Double
): State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(
        initialValue = null,
        contentScale,
        size,
        quality
    ) {
        value = size?.run {
            val newWidth = (width * quality).roundToInt()
            val newHeight = (height * quality).roundToInt()
            context.createThumbnail(resource.uri, newWidth, newHeight, type)?.asImageBitmap()
        }
    }
}

private suspend fun Context.createThumbnail(
    uri: Uri,
    width: Int,
    height: Int,
    type: MediaType
): Bitmap? = suspendCoroutine { continuation ->
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val cancelSignal = CancellationSignal().apply {
            setOnCancelListener {
                continuation.resume(value = null)
            }
        }

        val size = Size(width, height)
        contentResolver.loadThumbnail(uri, size, cancelSignal)
    } else {
        createLegacyThumbnail(uri, width, height, type)
    }

    continuation.resume(bitmap)
}

@Suppress("DEPRECATION")
private fun Context.createLegacyThumbnail(
    uri: Uri,
    width: Int,
    height: Int,
    type: MediaType
): Bitmap? {
    val bitmapOptions = BitmapFactory.Options().apply {
        outWidth = width
        outHeight = height
    }

    val mediaId = ContentUris.parseId(uri)
    return when (type) {
        VIDEO -> {
            MediaStore.Video.Thumbnails.getThumbnail(
                contentResolver,
                mediaId,
                MediaStore.Images.Thumbnails.MINI_KIND,
                bitmapOptions
            )
        }

        IMAGE -> {
            MediaStore.Images.Thumbnails.getThumbnail(
                contentResolver,
                mediaId,
                MediaStore.Images.Thumbnails.MINI_KIND,
                bitmapOptions
            )
        }
    }
}
