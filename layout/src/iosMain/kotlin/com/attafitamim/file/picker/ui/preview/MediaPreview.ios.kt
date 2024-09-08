package com.attafitamim.file.picker.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import com.attafitamim.file.picker.core.utils.thumbnailFromImageUrl
import com.attafitamim.file.picker.core.utils.thumbnailFromVideoUrl
import com.attafitamim.file.picker.ui.preview.MediaType.*
import com.attafitamim.file.picker.ui.utils.IMAGE_MAX_QUALITY
import com.attafitamim.file.picker.ui.utils.getThumbnailAsState
import com.attafitamim.file.picker.ui.utils.toImageBitmap
import platform.Photos.PHImageContentModeAspectFill
import platform.Photos.PHImageContentModeAspectFit
import platform.Photos.PHImageContentModeDefault

private const val VIDEO_TIME = 0.001

@Composable
actual fun MediaState.asBitmapState(
    contentScale: ContentScale,
    size: IntSize?
): State<ImageBitmap?> = when (resource.value) {
    is MediaStateResource.Value.Asset -> resource.value.asThumbnailBitmapState(contentScale, size)
    is MediaStateResource.Value.Path -> resource.value.asBitmapState(contentScale, size, type)
}

@Composable
private fun MediaStateResource.Value.Asset.asThumbnailBitmapState(
    contentScale: ContentScale,
    size: IntSize?
): State<ImageBitmap?> {
    val contentMode = remember(contentScale) {
        when (contentScale) {
            ContentScale.Fit -> PHImageContentModeAspectFit
            ContentScale.FillBounds,
            ContentScale.FillWidth,
            ContentScale.FillHeight -> PHImageContentModeAspectFill
            else -> PHImageContentModeDefault
        }
    }

    return asset.getThumbnailAsState(contentMode, size)
}

@Composable
private fun MediaStateResource.Value.Path.asBitmapState(
    contentScale: ContentScale,
    size: IntSize?,
    type: MediaType
): State<ImageBitmap?> = when (type) {
    VIDEO -> asVideoBitmapState(contentScale, size)
    IMAGE -> asImageBitmapState(contentScale, size)
}

@Composable
private fun MediaStateResource.Value.Path.asImageBitmapState(
    contentScale: ContentScale,
    size: IntSize?
): State<ImageBitmap?> = produceState<ImageBitmap?>(
    initialValue = null,
    uri,
    contentScale,
    size
) {
    size?.run {
        val uiImage = thumbnailFromImageUrl(
            uri = uri,
            width = width.toDouble(),
            height = height.toDouble(),
            isUrl = false
        )

        value = uiImage?.toImageBitmap(IMAGE_MAX_QUALITY)
    }
}

@Composable
private fun MediaStateResource.Value.Path.asVideoBitmapState(
    contentScale: ContentScale,
    size: IntSize?
): State<ImageBitmap?> = produceState<ImageBitmap?>(
    initialValue = null,
    uri,
    contentScale,
    size
) {
    size?.run {
        val uiImage = thumbnailFromVideoUrl(
            uri = uri,
            time = VIDEO_TIME,
            width = width.toDouble(),
            height = height.toDouble(),
            isUrl = false
        )

        value = uiImage?.toImageBitmap(IMAGE_MAX_QUALITY)
    }
}