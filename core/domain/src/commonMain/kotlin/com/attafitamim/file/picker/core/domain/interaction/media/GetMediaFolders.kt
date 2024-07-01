package com.attafitamim.file.picker.core.domain.interaction.media

import com.attafitamim.file.picker.core.domain.model.media.MediaFolder
import com.attafitamim.file.picker.core.domain.repository.media.IMediaRepository

class GetMediaFolders(private val mediaRepository: IMediaRepository) {

    suspend operator fun invoke(
        includeImages: Boolean,
        includeVideos: Boolean
    ): List<MediaFolder> = mediaRepository.getFolders(
        includeImages,
        includeVideos
    )
}
