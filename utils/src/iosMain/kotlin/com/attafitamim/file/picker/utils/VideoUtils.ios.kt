package com.attafitamim.file.picker.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVAssetImageGeneratorApertureModeEncodedPixels
import platform.AVFoundation.AVURLAsset
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL
import platform.UIKit.UIImage

private const val PREFERRED_TIMESCALE = 100
private const val MAX_QUALITY = 0.25

@Composable
actual fun rememberImageFromVideo(
    url: String,
    time: Double,
    isUrl: Boolean
): ImageBitmap? = remember(url, time) {
    imageFromVideo(url, time, isUrl)?.toImageBitmap(MAX_QUALITY)
}

@OptIn(ExperimentalForeignApi::class)
fun imageFromVideo(
    url: String,
    time: Double,
    isUrl: Boolean
): UIImage? = memScoped {
    runCatching {
        val nsUrl = if (isUrl) {
            NSURL(string = url)
        } else {
            NSURL(fileURLWithPath = url, isDirectory = false)
        }

        val asset = AVURLAsset(nsUrl, null)
        val assetIG = AVAssetImageGenerator(asset = asset)
        assetIG.appliesPreferredTrackTransform = true
        assetIG.apertureMode = AVAssetImageGeneratorApertureModeEncodedPixels

        val cmTime = CMTimeMakeWithSeconds(
            seconds = time,
            preferredTimescale = PREFERRED_TIMESCALE
        )

        val thumbnailImageRef = assetIG.copyCGImageAtTime(
            requestedTime = cmTime,
            actualTime = null,
            error = null
        )

        return UIImage(thumbnailImageRef)
    }.getOrNull()
}
