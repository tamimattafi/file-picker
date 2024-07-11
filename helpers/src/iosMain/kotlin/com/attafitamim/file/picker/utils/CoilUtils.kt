package com.attafitamim.file.picker.utils

import coil3.decode.DecodeUtils
import coil3.request.Options
import coil3.size.Dimension
import coil3.size.Precision
import coil3.size.Scale
import coil3.size.Size
import coil3.size.isOriginal
import coil3.size.pxOrElse
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.impl.use

// Taken from coil sources
fun Bitmap.Companion.makeFromImage(
    image: Image,
    options: Options,
): Bitmap {
    val srcWidth = image.width
    val srcHeight = image.height
    var multiplier = DecodeUtils.computeSizeMultiplier(
        srcWidth = srcWidth,
        srcHeight = srcHeight,
        dstWidth = options.size.widthPx(options.scale) { srcWidth },
        dstHeight = options.size.heightPx(options.scale) { srcHeight },
        scale = options.scale,
    )

    // Only upscale the image if the options require an exact size.
    if (options.precision == Precision.INEXACT) {
        multiplier = multiplier.coerceAtMost(1.0)
    }

    val dstWidth = (multiplier * srcWidth).toInt()
    val dstHeight = (multiplier * srcHeight).toInt()

    val bitmap = Bitmap()
    bitmap.allocN32Pixels(dstWidth, dstHeight)
    Canvas(bitmap).use { canvas ->
        canvas.drawImageRect(
            image = image,
            src = Rect.makeWH(srcWidth.toFloat(), srcHeight.toFloat()),
            dst = Rect.makeWH(dstWidth.toFloat(), dstHeight.toFloat()),
        )
    }
    return bitmap
}

inline fun Size.widthPx(scale: Scale, original: () -> Int): Int {
    return if (isOriginal) original() else width.toPx(scale)
}

inline fun Size.heightPx(scale: Scale, original: () -> Int): Int {
    return if (isOriginal) original() else height.toPx(scale)
}

fun Dimension.toPx(scale: Scale): Int = pxOrElse {
    when (scale) {
        Scale.FILL -> Int.MIN_VALUE
        Scale.FIT -> Int.MAX_VALUE
    }
}