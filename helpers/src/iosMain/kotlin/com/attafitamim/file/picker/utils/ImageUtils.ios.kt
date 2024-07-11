package com.attafitamim.file.picker.utils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.attafitamim.file.picker.core.utils.toByteArray
import com.attafitamim.file.picker.core.utils.useNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import org.jetbrains.skia.Image
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
    val image = useNSData(::UIImage)
    val jpeg = UIImageJPEGRepresentation(
        image,
        compressionQuality = compressionQuality
    )

    return requireNotNull(jpeg).toByteArray()
}
