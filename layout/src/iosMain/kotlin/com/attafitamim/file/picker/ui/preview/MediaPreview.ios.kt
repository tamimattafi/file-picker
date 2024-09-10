package com.attafitamim.file.picker.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import com.attafitamim.file.picker.core.utils.getThumbnail
import com.attafitamim.file.picker.core.utils.thumbnailFromImageUrl
import com.attafitamim.file.picker.core.utils.thumbnailFromVideoUrl
import com.attafitamim.file.picker.ui.preview.MediaType.*
import com.attafitamim.file.picker.ui.utils.IMAGE_MAX_QUALITY
import com.attafitamim.file.picker.ui.utils.toImageBitmap
import kotlin.math.roundToInt
import platform.Photos.PHImageContentModeAspectFill
import platform.Photos.PHImageContentModeAspectFit
import platform.Photos.PHImageContentModeDefault
import platform.UIKit.UIImage

private const val VIDEO_TIME = 0.001

@Composable
actual fun MediaState.asThumbnailBitmapState(
    contentScale: ContentScale,
    size: IntSize?,
    quality: Double
): State<ImageBitmap?> = produceState<ImageBitmap?>(
    initialValue = null,
    resource,
    contentScale,
    size,
    quality
) {
    value = size?.run {
        val newWidth = width * quality
        val newHeight = height * quality
        val bitmap = when (resource.value) {
            is MediaStateResource.Value.Asset -> resource.value.getThumbnail(
                contentScale,
                newWidth,
                newHeight
            )

            is MediaStateResource.Value.Path -> resource.value.getThumbnail(
                newWidth,
                newHeight,
                type
            )
        }

        bitmap?.toImageBitmap(IMAGE_MAX_QUALITY)
    }
}

private suspend fun MediaStateResource.Value.Asset.getThumbnail(
    contentScale: ContentScale,
    width: Double,
    height: Double
): UIImage? {
    val contentMode = when (contentScale) {
        ContentScale.Fit -> PHImageContentModeAspectFit
        ContentScale.FillBounds,
        ContentScale.FillWidth,
        ContentScale.FillHeight -> PHImageContentModeAspectFill
        else -> PHImageContentModeDefault
    }

    return asset.getThumbnail(
        width,
        height,
        contentMode
    )
}

private suspend fun MediaStateResource.Value.Path.getThumbnail(
    width: Double,
    height: Double,
    type: MediaType
): UIImage? = when (type) {
    VIDEO -> thumbnailFromVideoUrl(
        uri = uri,
        time = VIDEO_TIME,
        width = width,
        height = height,
        isUrl = false
    )

    IMAGE -> thumbnailFromImageUrl(
        uri = uri,
        width = width,
        height = height,
        isUrl = false
    )
}
