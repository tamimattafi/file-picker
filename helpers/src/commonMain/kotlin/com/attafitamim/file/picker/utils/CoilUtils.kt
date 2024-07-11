package com.attafitamim.file.picker.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.attafitamim.file.picker.utils.heic.HEICImageDecoderFactory

@Composable
fun rememberImageRequestWithHEICSupport(
    uri: String,
    compressionQuality: Double
): ImageRequest.Builder {
    val context = LocalPlatformContext.current

    return remember(uri, compressionQuality) {
        val builder = ImageRequest.Builder(context).data(uri)

        if (uri.isHEICUri()) {
            builder.decoderFactory { result, options, imageLoader ->
                HEICImageDecoderFactory(compressionQuality).create(result, options, imageLoader)
            }
        } else {
            builder
        }
    }
}
