package com.attafitamim.file.picker.core.data.source.media.element

sealed interface PlatformMediaElement {

    data class Image(
        val path: String?,
        val date: Double?
    ) : PlatformMediaElement

    data class Video(
        val path: String?,
        val date: Double?,
        val duration: Double
    ) : PlatformMediaElement
}
