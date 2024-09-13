package com.attafitamim.file.picker.core.domain.model.media

import platform.Photos.PHAsset

actual data class MediaResource(
    val value: Value
) {

    sealed interface Value {
        class Asset(
            val asset: PHAsset
        ) : Value {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as Asset

                return asset.localIdentifier == other.asset.localIdentifier
            }

            override fun hashCode(): Int {
                return asset.localIdentifier.hashCode()
            }
        }

        data class Path(
            val uri: String
        ) : Value
    }
}
