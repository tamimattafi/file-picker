package com.attafitamim.file.picker.core.data.local.media

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.attafitamim.file.picker.core.data.local.media.MediaHelper.insertImage
import com.attafitamim.file.picker.core.data.local.media.MediaHelper.insertMedia
import com.attafitamim.file.picker.core.data.source.media.IMediaLocalSource
import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * LocalSource for android sdk from version 30+
 */
@RequiresApi(Build.VERSION_CODES.R)
class MediaLocalSource(
    private val context: Context
) : IMediaLocalSource {

    override suspend fun getFolders(
        includeImages: Boolean,
        includeVideos: Boolean
    ): List<MediaFolder> = withContext(Dispatchers.IO) {
        if (!includeImages && !includeVideos) return@withContext emptyList()

        val selection = getStartSelection(includeImages, includeVideos)
        val initCursor = getInitCursor(selection)

        return@withContext getMediaFoldersForNewApi(initCursor)
    }

    override suspend fun getMediaElements(
        includeImages: Boolean,
        includeVideos: Boolean,
        descendingDirection: Boolean,
        mediaFolder: MediaFolder?,
        expectedSize: Int
    ): List<MediaElement> = withContext(Dispatchers.IO) {
        if (!includeImages && !includeVideos) return@withContext emptyList()

        val mediaCursor = getMediaCursor(
            mediaFolder,
            descendingDirection,
            includeImages,
            includeVideos
        )

        return@withContext getMediaElementsForNewApi(
            mediaCursor,
            mediaFolder,
            expectedSize
        )
    }

    override suspend fun addImage(
        imageBytes: ByteArray,
        title: String,
        currentTime: Long,
        mimeType: String,
        description: String?,
        isDateEnabled: Boolean
    ): MediaElement.ImageElement = context.insertImage(
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
    ): MediaElement = context.insertMedia(
        path,
        mimeType,
        title,
        currentTime,
        description,
        isDateEnabled,
        isPhoto
    )

    private fun getStartSelection(
        includeImages: Boolean,
        includeVideos: Boolean
    ) = if (!includeImages && !includeVideos) {
        null
    } else {
        buildString {
            if (includeImages) {
                append(
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    SELECTION_LIKE_TEMPLATE,
                    IMAGE_MIMETYPE
                )
            }

            if (includeImages && includeVideos) {
                append(OR_TEMPLATE)
            }

            if (includeVideos) {
                append(
                    MediaStore.Files.FileColumns.MIME_TYPE,
                    SELECTION_LIKE_TEMPLATE,
                    VIDEO_MIMETYPE
                )
            }
        }
    }

    private fun getMediaCursor(
        mediaFolder: MediaFolder?,
        descendingDirection: Boolean,
        includeImages: Boolean,
        includeVideos: Boolean
    ): Cursor {
        val startSelection = getStartSelection(includeImages, includeVideos)
        val selection = getFullSelection(mediaFolder, startSelection)
        val sortOrdering = getSortOrdering(descendingDirection)

        return getCursor(
            PROJECTION,
            selection,
            sortOrdering
        )
    }

    private fun getSortOrdering(
        descendingDirection: Boolean
    ): String {
        val direction = if (descendingDirection) DESCENDING_ORDERING else ASCENDING_ORDERING

        return "$ORDERING_COLUMN $direction"
    }

    private fun getFullSelection(
        mediaFolder: MediaFolder?,
        startSelection: String?
    ): String? = if (startSelection == null) {
        null
    } else {
        buildString {
            if (mediaFolder != null) {
                append(START_BRACKET)
            }

            append(startSelection)

            if (mediaFolder != null) {
                append(END_BRACKET)
                append(
                    AND_TEMPLATE,
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    SELECTION_LIKE_TEMPLATE,
                    "\'%${mediaFolder.name}%\'",
                )
            }
        }
    }

    private fun getInitCursor(
        selection: String?
    ) = getCursor(
        INIT_PROJECTION,
        selection,
        INIT_SORT_ORDERING
    )

    private fun getCursor(
        projection: Array<String>,
        selection: String?,
        sortOrdering: String
    ) = context.contentResolver.query(
        MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL),
        projection,
        selection,
        null,
        sortOrdering,
        null
    ) ?: error("Cannot create cursor")

    private companion object {

        val INIT_PROJECTION = arrayOf(
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.MIME_TYPE
        )

        val PROJECTION = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.RELATIVE_PATH,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DURATION
        )

        const val DESCENDING_ORDERING = "DESC"
        const val ASCENDING_ORDERING = "ASC"
        const val ORDERING_COLUMN = MediaStore.MediaColumns.DATE_MODIFIED

        const val INIT_ORDERING_COLUMN = MediaStore.MediaColumns.RELATIVE_PATH
        const val INIT_ORDERING_DIRECTION = DESCENDING_ORDERING
        const val INIT_SORT_ORDERING = "$INIT_ORDERING_COLUMN $INIT_ORDERING_DIRECTION"

        const val SELECTION_LIKE_TEMPLATE = " LIKE "
        const val VIDEO_MIMETYPE = "\'video%\'"
        const val IMAGE_MIMETYPE = "\'image%\'"
        const val OR_TEMPLATE = " OR "
        const val AND_TEMPLATE = " AND "
        const val START_BRACKET = '('
        const val END_BRACKET = ')'
    }
}
