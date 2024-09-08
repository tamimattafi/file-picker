package com.attafitamim.file.picker.core.utils

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAsset
import platform.AVFoundation.AVAssetImageGenerator
import platform.AVFoundation.AVAssetImageGeneratorApertureModeEncodedPixels
import platform.AVFoundation.AVURLAsset
import platform.CoreGraphics.CGImageRelease
import platform.CoreGraphics.CGSizeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSURL
import platform.UIKit.UIImage

private const val PREFERRED_TIMESCALE = 100

fun thumbnailFromVideoUrl(
    uri: String,
    time: Double,
    isUrl: Boolean,
    width: Double,
    height: Double,
    timescale: Int = PREFERRED_TIMESCALE
): UIImage? = runCatching {
    val nsUrl = if (isUrl) {
        NSURL(string = uri)
    } else {
        NSURL(fileURLWithPath = uri, isDirectory = false)
    }

    val asset = AVURLAsset(nsUrl, null)
    asset.getThumbnail(time, width, height, timescale)
}.getOrNull()

@OptIn(ExperimentalForeignApi::class)
fun AVAsset.getThumbnail(
    time: Double,
    width: Double,
    height: Double,
    timescale: Int = PREFERRED_TIMESCALE
): UIImage? = runCatching {
    val assetIG = AVAssetImageGenerator(asset = this)
    assetIG.appliesPreferredTrackTransform = true
    assetIG.apertureMode = AVAssetImageGeneratorApertureModeEncodedPixels
    assetIG.maximumSize = CGSizeMake(width, height)

    val cmTime = CMTimeMakeWithSeconds(
        seconds = time,
        preferredTimescale = timescale
    )

    val thumbnailImageRef = assetIG.copyCGImageAtTime(
        requestedTime = cmTime,
        actualTime = null,
        error = null
    )

    val uiImage = UIImage(thumbnailImageRef)
    CGImageRelease(thumbnailImageRef)
    uiImage
}.getOrNull()