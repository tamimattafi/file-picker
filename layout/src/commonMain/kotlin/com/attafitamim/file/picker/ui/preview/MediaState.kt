package com.attafitamim.file.picker.ui.preview

import androidx.compose.runtime.Stable

@Stable
expect class MediaStateResource

@Stable
data class MediaState(
    val resource: MediaStateResource,
    val type: MediaType
)

@Stable
enum class MediaType {
    VIDEO,
    IMAGE
}