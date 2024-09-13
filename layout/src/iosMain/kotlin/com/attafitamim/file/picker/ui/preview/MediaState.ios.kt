package com.attafitamim.file.picker.ui.preview

import androidx.compose.runtime.Stable
import platform.Photos.PHAsset

@Stable
actual data class MediaStateResource(
    val value: Value
) {

    @Stable
    sealed interface Value {

        @Stable
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

        @Stable
        data class Path(
            val uri: String
        ) : Value
    }
}