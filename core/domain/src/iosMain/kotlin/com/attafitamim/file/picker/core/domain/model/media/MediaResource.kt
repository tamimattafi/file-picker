package com.attafitamim.file.picker.core.domain.model.media

import platform.Photos.PHAsset

actual class MediaResource(
    val value: Value
) {

    sealed interface Value {
        class Asset(
            val asset: PHAsset
        ) : Value

        class Path(
            val uri: String
        ) : Value
    }
}
