package com.attafitamim.file.picker.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.IntSize
import com.attafitamim.file.picker.core.utils.getThumbnail
import com.attafitamim.file.picker.core.utils.toByteArray
import com.attafitamim.file.picker.core.utils.toNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import org.jetbrains.skia.Image
import platform.Foundation.NSURL
import platform.Photos.PHAsset
import platform.Photos.PHImageContentMode
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation

actual fun ByteArray.toImageBitmap(): ImageBitmap? = runCatching {
    return Image.makeFromEncoded(this).toComposeImageBitmap()
}.getOrNull()

fun UIImage.toImageBitmap(quality: Double): ImageBitmap? = runCatching {
    val pngData = UIImageJPEGRepresentation(this, quality)!!
    pngData.toByteArray().toImageBitmap()
}.getOrNull()

@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toJpegBytes(compressionQuality: Double): ByteArray = memScoped {
    val image = UIImage(toNSData())
    val jpeg = UIImageJPEGRepresentation(
        image,
        compressionQuality = compressionQuality
    )

    return requireNotNull(jpeg).toByteArray()
}

fun String.pathToImage(isDirectory: Boolean = false): UIImage? {
    val nsPath = NSURL.fileURLWithPath(this, isDirectory).path ?: return null
    return UIImage.imageWithContentsOfFile(nsPath)
}

fun String.pathToImageBitmap(quality: Double = IMAGE_MAX_QUALITY): ImageBitmap? =
    pathToImage()?.toImageBitmap(quality)
