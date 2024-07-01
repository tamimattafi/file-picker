package com.attafitamim.file.picker.core.domain.repository.media

import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder

interface IMediaRepository {

    suspend fun getFolders(
        includeImages: Boolean,
        includeVideos: Boolean
    ): List<MediaFolder>

    suspend fun getMediaElements(
        includeImages: Boolean,
        includeVideos: Boolean,
        descendingDirection: Boolean,
        mediaFolder: MediaFolder?,
        expectedSize: Int,
    ): List<MediaElement>
}
