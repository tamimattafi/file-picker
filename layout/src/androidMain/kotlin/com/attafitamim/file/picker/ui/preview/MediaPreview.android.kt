package com.attafitamim.file.picker.ui.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import java.io.IOException


private const val VIDEO_FRAME_SECOND = 1L

@Composable
actual fun MediaState.asBitmapState(
    contentScale: ContentScale,
    size: IntSize?
): State<ImageBitmap?> {
    val context = LocalContext.current
    return produceState<ImageBitmap?>(
        initialValue = null,
        contentScale,
        size
    ) {
        val bitmap = when (type) {
            MediaType.VIDEO -> context.extractFrameFromVideo(resource.uri)
            MediaType.IMAGE -> context.getBitmap(resource.uri)
        }

        val thumbnail = size?.runCatching {
            ThumbnailUtils.extractThumbnail(bitmap, width, height)
        }?.getOrNull() ?: bitmap

        value = thumbnail?.asImageBitmap()
    }
}

private fun Context.getBitmap(uri: Uri): Bitmap? = try {
    contentResolver.openInputStream(uri).use { inputStream ->
        BitmapFactory.decodeStream(inputStream)
    }
} catch (ex: IOException) {
    null
}

private fun Context.extractFrameFromVideo(
    uri: Uri
): Bitmap? {
    val mediaMetadataRetriever = MediaMetadataRetriever()
    return try {
        contentResolver.openAssetFileDescriptor(uri, "r")?.use { fd ->
            mediaMetadataRetriever.setDataSource(fd.fileDescriptor)
        }

        mediaMetadataRetriever.getFrameAtTime(
            VIDEO_FRAME_SECOND,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        )
    } catch (_: Exception) {
        null
    } finally {
        mediaMetadataRetriever.release()
    }
}
