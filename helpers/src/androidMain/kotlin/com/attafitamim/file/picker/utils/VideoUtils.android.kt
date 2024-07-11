package com.attafitamim.file.picker.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.attafitamim.file.picker.core.utils.SECOND_IN_MILLIS
import kotlin.math.roundToLong

private const val VIDEO_FRAME_SECOND = 1L

@Composable
actual fun rememberImageFromVideo(
    url: String,
    time: Double,
    isUrl: Boolean,
    quality: Double
): ImageBitmap? {
    val context = LocalContext.current
    return remember(url, time) {
        val timeInMillis = (time * SECOND_IN_MILLIS).roundToLong()
        val actualTime = timeInMillis.takeUnless {
            timeInMillis < VIDEO_FRAME_SECOND
        } ?: VIDEO_FRAME_SECOND

        imageFromVideo(url, actualTime, isUrl, context)?.asImageBitmap()
    }
}

fun imageFromVideo(
    url: String,
    time: Long,
    isUrl: Boolean,
    context: Context
): Bitmap? {
    val mediaMetadataRetriever = MediaMetadataRetriever()

    val result = runCatching {
        if (isUrl) {
            mediaMetadataRetriever.setDataSource(url)
        } else {
            context.contentResolver.openAssetFileDescriptor(
                Uri.parse(url),
                "r"
            )!!.use { fd ->
                mediaMetadataRetriever.setDataSource(fd.fileDescriptor)
            }
        }

        mediaMetadataRetriever.getFrameAtTime(
            time,
            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
        )
    }

    mediaMetadataRetriever.release()
    return result.getOrNull()
}
