package com.attafitamim.file.picker.core.data.local.media.oldapi

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import com.attafitamim.file.picker.core.data.local.media.MediaHelper.insertImage
import com.attafitamim.file.picker.core.data.local.media.MediaHelper.insertMedia
import com.attafitamim.file.picker.core.data.source.media.IMediaLocalSource
import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder
import com.attafitamim.file.picker.core.domain.model.media.MediaLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * LocalSource for android sdk up to version 29
 */
class MediaLocalSourceLegacy(
    private val context: Context,
    private val appFolder: String
) : IMediaLocalSource {

    override suspend fun getFolders(
        includeImages: Boolean,
        includeVideos: Boolean
    ): List<MediaFolder> = withContext(Dispatchers.IO) {
        if (!includeImages && !includeVideos) return@withContext emptyList()

        val imagesCursor = if (includeImages) getInitImagesCursor() else null
        val videosCursor = if (includeVideos) getInitVideosCursor() else null

        return@withContext getMediaFoldersForOldApi(
            imagesCursor,
            videosCursor
        )
    }

    override suspend fun getMediaElements(
        includeImages: Boolean,
        includeVideos: Boolean,
        descendingDirection: Boolean,
        mediaFolder: MediaFolder?,
        expectedSize: Int
    ): List<MediaElement> = withContext(Dispatchers.IO) {
        if (!includeVideos && !includeImages) return@withContext emptyList()

        val imagesCursor = getImagesCursor(mediaFolder, descendingDirection)
        val videosCursor = getVideosCursor(mediaFolder, descendingDirection)

        return@withContext getMediaElementsForOldApi(
            context,
            imagesCursor,
            videosCursor,
            includeImages,
            includeVideos,
            mediaFolder,
            expectedSize,
        )
    }

    override suspend fun addImage(
        imageBytes: ByteArray,
        title: String,
        currentTime: Long,
        mimeType: String,
        description: String?,
        isDateEnabled: Boolean
    ): MediaLocation = context.insertImage(
        imageBytes,
        title,
        currentTime,
        mimeType,
        description,
        isDateEnabled,
        appFolder
    )

    override suspend fun addMedia(
        path: String,
        mimeType: String,
        title: String,
        currentTime: Long,
        description: String?,
        isDateEnabled: Boolean,
        isPhoto: Boolean
    ): MediaLocation = context.insertMedia(
        path,
        mimeType,
        title,
        currentTime,
        description,
        isDateEnabled,
        isPhoto,
        appFolder
    )

    private fun getInitImagesCursor() = getInitCursor(
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    )

    private fun getInitVideosCursor() = getInitCursor(
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    )

    private fun getImagesCursor(
        mediaFolder: MediaFolder?,
        descendingDirection: Boolean
    ) = getMediaCursor(
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        mediaFolder,
        descendingDirection
    )

    private fun getVideosCursor(
        mediaFolder: MediaFolder?,
        descendingDirection: Boolean
    ) = getMediaCursor(
        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        mediaFolder,
        descendingDirection
    )

    private fun getSortOrdering(
        descendingDirection: Boolean
    ): String {
        val direction = if (descendingDirection) DESCENDING_ORDERING else ASCENDING_ORDERING

        return "$ORDERING_COLUMN $direction"
    }

    private fun getSelectionForFolder(mediaFolder: MediaFolder?) = mediaFolder?.let { folder ->
        buildString {
            append(
                MediaStore.MediaColumns.DATA,
                SELECTION_LIKE_TEMPLATE,
                SELECTION_START_TEMPLATE,
                folder.name,
                SELECTION_END_TEMPLATE
            )
        }
    }

    private fun getMediaCursor(
        uri: Uri,
        mediaFolder: MediaFolder?,
        descendingDirection: Boolean
    ): Cursor {
        val selection = getSelectionForFolder(mediaFolder)
        val sortOrdering = getSortOrdering(descendingDirection)

        return getCursor(
            uri,
            PROJECTION,
            selection,
            sortOrdering
        )
    }

    private fun getInitCursor(
        uri: Uri
    ) = getCursor(
        uri,
        INIT_PROJECTION,
        selection = null,
        INIT_SORT_ORDERING
    )

    private fun getCursor(
        uri: Uri,
        projection: Array<String>,
        selection: String?,
        sortOrdering: String
    ) = context.contentResolver.query(
        uri,
        projection,
        selection,
        null,
        sortOrdering,
        null
    ) ?: error("Cannot create cursor")

    private companion object {

        val INIT_PROJECTION = arrayOf(MediaStore.MediaColumns.DATA)

        val PROJECTION = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATA
        )

        const val INIT_ORDERING_COLUMN = MediaStore.MediaColumns.DATA
        const val DESCENDING_ORDERING = "DESC"
        const val ASCENDING_ORDERING = "ASC"
        const val ORDERING_COLUMN = MediaStore.MediaColumns.DATE_MODIFIED

        const val INIT_ORDERING_DIRECTION = DESCENDING_ORDERING
        const val INIT_SORT_ORDERING = "$INIT_ORDERING_COLUMN $INIT_ORDERING_DIRECTION"
        const val SELECTION_LIKE_TEMPLATE = " LIKE "
        const val SELECTION_START_TEMPLATE = "\'%"
        const val SELECTION_END_TEMPLATE = "%\'"
    }
}
