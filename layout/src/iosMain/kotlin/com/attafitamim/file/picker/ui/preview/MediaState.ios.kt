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
        ) : Value

        @Stable
        class Path(
            val uri: String
        ) : Value
    }
}