package com.attafitamim.file.picker.core.data.repository.media

import com.attafitamim.file.picker.core.data.source.media.IMediaLocalSource
import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder
import com.attafitamim.file.picker.core.domain.repository.media.IMediaRepository

class MediaRepository(private val localSource: IMediaLocalSource) : IMediaRepository {

    override suspend fun getFolders(
        includeImages: Boolean,
        includeVideos: Boolean
    ): List<MediaFolder> = localSource.getFolders(
        includeImages,
        includeVideos
    )

    override suspend fun getMediaElements(
        includeImages: Boolean,
        includeVideos: Boolean,
        descendingDirection: Boolean,
        mediaFolder: MediaFolder?,
        expectedSize: Int
    ): List<MediaElement> = localSource.getMediaElements(
        includeImages,
        includeVideos,
        descendingDirection,
        mediaFolder,
        expectedSize
    )
}
