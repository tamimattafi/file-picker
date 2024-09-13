package com.attafitamim.file.picker.core.utils

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVURLAsset
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreGraphics.CGSizeMake
import platform.CoreServices.UTTypeCopyPreferredTagWithClass
import platform.CoreServices.kUTTagClassMIMEType
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSString
import platform.Photos.PHAsset
import platform.Photos.PHAssetMediaTypeImage
import platform.Photos.PHAssetMediaTypeVideo
import platform.Photos.PHAssetResource
import platform.Photos.PHContentEditingInputRequestOptions
import platform.Photos.PHImageContentMode
import platform.Photos.PHImageContentModeDefault
import platform.Photos.PHImageManager
import platform.Photos.PHImageRequestOptions
import platform.Photos.PHVideoRequestOptions
import platform.Photos.PHVideoRequestOptionsVersionOriginal
import platform.Photos.requestContentEditingInputWithOptions
import platform.UIKit.UIImage

@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
@ExperimentalForeignApi
val PHAsset.mimeType: String?
    get() = runCatching {
        val uType = PHAssetResource.assetResourcesForAsset(asset = this)
            .firstNotNullOfOrNull { resource -> resource as? PHAssetResource }
            ?.uniformTypeIdentifier as? NSString

        uType?.let { type ->
            val cfUType = CFBridgingRetain(type) as? CFStringRef
            val cfMimeType = UTTypeCopyPreferredTagWithClass(cfUType, kUTTagClassMIMEType)
            CFRelease(cfUType)
            CFBridgingRelease(cfMimeType) as String
        }?.takeIf(String::isNotBlank)
    }.getOrNull()

suspend fun PHAsset.getPath(): String? = suspendCoroutine { continuation ->
    when (mediaType) {
        PHAssetMediaTypeImage -> {
            val options = PHContentEditingInputRequestOptions()
            options.setNetworkAccessAllowed(true)
            options.setCanHandleAdjustmentData {
                true
            }

            requestContentEditingInputWithOptions(options) { contentEditingInput, _ ->
                val path = contentEditingInput?.fullSizeImageURL?.path?.takeIf(String::isNotBlank)
                continuation.resume(path)
            }
        }

        PHAssetMediaTypeVideo -> {
            val options = PHVideoRequestOptions()
            options.version = PHVideoRequestOptionsVersionOriginal
            options.setNetworkAccessAllowed(true)
            PHImageManager.defaultManager().requestAVAssetForVideo(
                asset = this,
                options = options
            ) { asset, _, _ ->
                val urlAsset = asset as? AVURLAsset
                val path = urlAsset?.URL?.path?.takeIf(String::isNotBlank)
                continuation.resume(path)
            }
        }

        else -> error("mediaType is not supported: $mediaType")
    }
}

@OptIn(ExperimentalForeignApi::class)
suspend fun PHAsset.getThumbnail(
    width: Double,
    height: Double,
    contentMode: PHImageContentMode = PHImageContentModeDefault
): UIImage? = suspendCoroutine { continuation ->
    val options = PHImageRequestOptions()
    options.setNetworkAccessAllowed(true)
    options.synchronous = true

    val size = CGSizeMake(width = width, height = height)
    PHImageManager.defaultManager().requestImageForAsset(
        asset = this,
        targetSize = size,
        contentMode = contentMode,
        options = options
    ) { uiImage, _ ->
        continuation.resume(uiImage)
    }
}
