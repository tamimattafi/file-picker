package com.attafitamim.file.picker.core.domain.interaction.media

import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder
import com.attafitamim.file.picker.core.domain.repository.media.IMediaRepository

class GetMediaElements(private val mediaRepository: IMediaRepository) {

    suspend operator fun invoke(
        includeImages: Boolean,
        includeVideos: Boolean,
        expectedSize: Int,
        mediaFolder: MediaFolder? = null,
        descendingDirection: Boolean = true,
    ): List<MediaElement> = mediaRepository.getMediaElements(
        includeImages,
        includeVideos,
        descendingDirection,
        mediaFolder,
        expectedSize
    )
}
