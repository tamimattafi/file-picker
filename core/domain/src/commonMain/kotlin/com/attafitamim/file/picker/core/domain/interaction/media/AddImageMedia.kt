package com.attafitamim.file.picker.core.domain.interaction.media

import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.RawMedia
import com.attafitamim.file.picker.core.domain.repository.media.IMediaRepository
import com.attafitamim.file.picker.core.utils.currentTimeInMillis

class AddImageMedia(
    private val mediaRepository: IMediaRepository
) {

    suspend operator fun invoke(
        rawMedia: RawMedia,
        title: String,
        mimeType: String,
        time: Long? = null,
        description: String? = null,
        isDateEnabled: Boolean = true
    ): MediaElement {
        val currentTime = time ?: currentTimeInMillis()

        return mediaRepository.addImage(
            rawMedia.bytes,
            title,
            currentTime,
            mimeType,
            description,
            isDateEnabled
        )
    }
}
