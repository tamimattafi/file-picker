package com.attafitamim.file.picker.utils.heic

import org.jetbrains.skia.Bitmap
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.decode.DecodeResult
import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.request.Options
import com.attafitamim.file.picker.utils.makeFromImage
import com.attafitamim.file.picker.utils.toJpegBytes
import okio.use
import org.jetbrains.skia.Image

actual class HEICImageDecoder actual constructor(
    private val source: ImageSource,
    private val options: Options,
    private val compressionQuality: Double,
) : Decoder {

    @OptIn(ExperimentalCoilApi::class)
    override suspend fun decode(): DecodeResult {
        val originalBytes = source.source().use { it.readByteArray() }

        val jpegBytes = originalBytes.toJpegBytes(
            compressionQuality = compressionQuality
        )

        val image = Image.makeFromEncoded(jpegBytes)

        val isSampled: Boolean
        val bitmap: Bitmap
        try {
            bitmap = Bitmap.makeFromImage(image, options)
            bitmap.setImmutable()
            isSampled = bitmap.width < image.width || bitmap.height < image.height
        } finally {
            image.close()
        }

        return DecodeResult(
            image = bitmap.asImage(),
            isSampled = isSampled,
        )
    }
}

