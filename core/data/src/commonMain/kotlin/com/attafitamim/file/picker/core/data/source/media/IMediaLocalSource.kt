package com.attafitamim.file.picker.core.data.source.media

import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder
import com.attafitamim.file.picker.core.domain.model.media.MediaLocation

interface IMediaLocalSource {

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

    suspend fun addImage(
        imageBytes: ByteArray,
        title: String,
        currentTime: Long,
        mimeType: String,
        description: String?,
        isDateEnabled: Boolean
    ): MediaLocation

    suspend fun addMedia(
        path: String,
        mimeType: String,
        title: String,
        currentTime: Long,
        description: String?,
        isDateEnabled: Boolean,
        isPhoto: Boolean
    ): MediaLocation
}
