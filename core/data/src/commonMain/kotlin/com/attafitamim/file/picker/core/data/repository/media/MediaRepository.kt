package com.attafitamim.file.picker.core.data.repository.media

import com.attafitamim.file.picker.core.data.source.media.IMediaLocalSource
import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder
import com.attafitamim.file.picker.core.domain.model.media.MediaLocation
import com.attafitamim.file.picker.core.domain.repository.media.IMediaRepository

class MediaRepository(
    private val localSource: IMediaLocalSource
) : IMediaRepository {

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

    override suspend fun addImage(
        imageBytes: ByteArray,
        title: String,
        currentTime: Long,
        mimeType: String,
        description: String?,
        isDateEnabled: Boolean
    ): MediaLocation = localSource.addImage(
        imageBytes,
        title,
        currentTime,
        mimeType,
        description,
        isDateEnabled
    )

    override suspend fun addMedia(
        path: String,
        mimeType: String,
        title: String,
        currentTime: Long,
        description: String?,
        isDateEnabled: Boolean,
        isPhoto: Boolean
    ): MediaLocation = localSource.addMedia(
        path,
        mimeType,
        title,
        currentTime,
        description,
        isDateEnabled,
        isPhoto
    )
}
