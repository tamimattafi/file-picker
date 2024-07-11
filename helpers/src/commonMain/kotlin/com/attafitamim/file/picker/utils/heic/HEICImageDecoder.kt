package com.attafitamim.file.picker.utils.heic

import coil3.decode.Decoder
import coil3.decode.ImageSource
import coil3.request.Options

expect class HEICImageDecoder(
    source: ImageSource,
    options: Options,
    compressionQuality: Double
) : Decoder
