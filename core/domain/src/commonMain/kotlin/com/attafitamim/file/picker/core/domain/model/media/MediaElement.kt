package com.attafitamim.file.picker.core.domain.model.media

sealed interface MediaElement {
    val resource: MediaResource
    val mimeType: String?
    val modifiedDateInSeconds: Int

    data class ImageElement(
        override val resource: MediaResource,
        override val mimeType: String?,
        override val modifiedDateInSeconds: Int,
    ) : MediaElement

    /**
     * @param duration can be null if something in device goes wrong. In milliseconds.
     */
    data class VideoElement(
        override val resource: MediaResource,
        override val mimeType: String?,
        override val modifiedDateInSeconds: Int,
        val duration: Long?,
    ) : MediaElement
}
