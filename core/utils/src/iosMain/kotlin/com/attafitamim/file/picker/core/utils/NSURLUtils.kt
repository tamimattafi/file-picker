package com.attafitamim.file.picker.core.utils

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSURL
import platform.QuickLookThumbnailing.QLThumbnailGenerationRequest
import platform.QuickLookThumbnailing.QLThumbnailGenerationRequestRepresentationTypeThumbnail
import platform.QuickLookThumbnailing.QLThumbnailGenerator
import platform.UIKit.UIImage

suspend fun thumbnailFromImageUrl(
    uri: String,
    isUrl: Boolean,
    width: Double,
    height: Double,
    scale: Double = 1.0
): UIImage? {
    val nsUrl = if (isUrl) {
        NSURL(string = uri)
    } else {
        NSURL(fileURLWithPath = uri, isDirectory = false)
    }

    return nsUrl.getThumbnail(width, height, scale)
}

@OptIn(ExperimentalForeignApi::class)
suspend fun NSURL.getThumbnail(
    width: Double,
    height: Double,
    scale: Double = 1.0
): UIImage? = suspendCoroutine { continuation ->
    val previewGenerator = QLThumbnailGenerator()
    val thumbnailSize = CGSizeMake(width, height)
    val request = QLThumbnailGenerationRequest(
        fileAtURL = this,
        size = thumbnailSize,
        scale = scale,
        representationTypes = QLThumbnailGenerationRequestRepresentationTypeThumbnail
    )

    previewGenerator.generateRepresentationsForRequest(request) { thumbnail, _, _ ->
        val imageRef = thumbnail?.CGImage
        val uiImage = imageRef?.let(::UIImage)
        CGImageRelease(imageRef)
        continuation.resume(uiImage)
    }
}
