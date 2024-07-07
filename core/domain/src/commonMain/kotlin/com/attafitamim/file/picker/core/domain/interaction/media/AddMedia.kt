package com.attafitamim.file.picker.core.domain.interaction.media

import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.repository.media.IMediaRepository
import com.attafitamim.file.picker.core.utils.currentTimeInMillis

class AddMedia(
    private val mediaRepository: IMediaRepository
) {

    suspend operator fun invoke(
        path: String,
        mimeType: String,
        title: String,
        isPhoto: Boolean,
        time: Long? = null,
        description: String? = null,
        isDateEnabled: Boolean = true
    ): MediaElement {
        val currentTime = time ?: currentTimeInMillis()

        return mediaRepository.addMedia(
            path,
            mimeType,
            title,
            currentTime,
            description,
            isDateEnabled,
            isPhoto
        )
    }
}
