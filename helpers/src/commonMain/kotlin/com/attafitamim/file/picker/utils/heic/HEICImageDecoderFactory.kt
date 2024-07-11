package com.attafitamim.file.picker.utils.heic

import coil3.ImageLoader
import coil3.decode.Decoder
import coil3.fetch.SourceFetchResult
import coil3.request.Options

class HEICImageDecoderFactory(
    private val compressionQuality: Double
) : Decoder.Factory {

    override fun create(
        result: SourceFetchResult,
        options: Options,
        imageLoader: ImageLoader,
    ): Decoder = HEICImageDecoder(result.source, options, compressionQuality)
}
