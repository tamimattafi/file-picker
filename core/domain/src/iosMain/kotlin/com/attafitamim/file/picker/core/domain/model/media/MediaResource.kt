package com.attafitamim.file.picker.core.domain.model.media

import platform.Photos.PHAsset

actual data class MediaResource(
    val value: Value
) {

    sealed interface Value {
        data class Asset(
            val asset: PHAsset
        ) : Value

        data class Path(
            val uri: String
        ) : Value
    }
}
