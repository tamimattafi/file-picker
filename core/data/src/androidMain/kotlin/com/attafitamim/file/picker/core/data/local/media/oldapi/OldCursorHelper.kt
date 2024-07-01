package com.attafitamim.file.picker.core.data.local.media.oldapi

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder

private const val SEPARATOR = '/'
private const val DOT = '.'
private const val FILE_DESCRIPTOR_MODE = "r"

fun getMediaElementsForOldApi(
    context: Context,
    imagesCursor: Cursor,
    videosCursor: Cursor,
    includeImages: Boolean,
    includeVideos: Boolean,
    mediaFolder: MediaFolder?,
    expectedSize: Int,
): List<MediaElement> {
    val retriever = MediaMetadataRetriever()

    imagesCursor.moveToNext()
    videosCursor.moveToNext()

    var canLoadImages = includeImages
    var canLoadVideos = includeVideos

    val imagesIdColumnIndex =
        imagesCursor.getColumnIndex(MediaStore.MediaColumns._ID)
    val imagesModifiedDateColumnIndex =
        imagesCursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
    val imagesMimeColumnIndex =
        imagesCursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
    val imagesDataColumnIndex = imagesCursor.getColumnIndex(MediaStore.MediaColumns.DATA)

    val videosIdColumnIndex =
        videosCursor.getColumnIndex(MediaStore.MediaColumns._ID)
    val videosModifiedDateColumnIndex =
        videosCursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
    val videosMimeColumnIndex =
        videosCursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
    val videosDataColumnIndex = videosCursor.getColumnIndex(MediaStore.MediaColumns.DATA)

    val mediaElements = ArrayList<MediaElement>(expectedSize)
    while (mediaElements.size <= expectedSize) {
        if (imagesCursor.isAfterLast) {
            canLoadImages = false
        }
        if (videosCursor.isAfterLast) {
            canLoadVideos = false
        }

        if (!canLoadImages && !canLoadVideos) {
            break
        }

        val imagesDate = if (canLoadImages) {
            imagesCursor.getInt(imagesModifiedDateColumnIndex)
        } else {
            null
        }

        val videosDate = if (canLoadVideos) {
            videosCursor.getInt(videosModifiedDateColumnIndex)
        } else {
            null
        }

        if (imagesDate == null && videosDate == null) {
            break
        }

        if (imagesDate != null && (videosDate == null || videosDate < imagesDate)) {
            // load images
            if (mediaFolder != null) {
                val imagesData = imagesCursor.getString(imagesDataColumnIndex)
                if (skipElementIfNotValid(imagesData, mediaFolder.name)) {
                    imagesCursor.moveToNext()
                    continue
                }
            }

            val id = imagesCursor.getString(imagesIdColumnIndex).toLong()
            val mimeType = imagesCursor.getString(imagesMimeColumnIndex)
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                .toString()

            MediaElement.ImageElement(
                uri,
                mimeType,
                imagesDate
            ).let(mediaElements::add)

            imagesCursor.moveToNext()
        } else if (videosDate != null) {
            if (mediaFolder != null) {
                val videosData = videosCursor.getString(videosDataColumnIndex)
                if (skipElementIfNotValid(videosData, mediaFolder.name)) {
                    videosCursor.moveToNext()
                    continue
                }
            }

            val id = videosCursor.getString(videosIdColumnIndex).toLong()
            val mimeType = videosCursor.getString(videosMimeColumnIndex)
            val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

            try {
                context.contentResolver.openAssetFileDescriptor(
                    uri,
                    FILE_DESCRIPTOR_MODE
                )!!.use { fileDescriptor ->
                    retriever.setDataSource(fileDescriptor.fileDescriptor)
                }
            } catch (_: Exception) {
                videosCursor.moveToNext()
                continue
            }

            val durationInMillis = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLong()

            MediaElement.VideoElement(
                uri.toString(),
                mimeType,
                videosDate,
                durationInMillis
            ).let(mediaElements::add)

            videosCursor.moveToNext()
        }
    }

    imagesCursor.close()
    videosCursor.close()
    return mediaElements
}

fun getMediaFoldersForOldApi(
    imagesCursor: Cursor?,
    videosCursor: Cursor?
): List<MediaFolder> {
    val directories = HashSet<String>()
    var currentFolder = ""
    if (imagesCursor != null) {
        val imagesDataColumnIndex = imagesCursor.getColumnIndex(MediaStore.MediaColumns.DATA)
        // Image cursor
        while (imagesCursor.moveToNext()) {
            val imagesData = imagesCursor.getString(imagesDataColumnIndex)
            val folderName = getFolderName(imagesData)
            if (!currentFolder.equals(folderName, true)) {
                currentFolder = folderName
                directories.add(folderName)
            }
        }
    }

    if (videosCursor != null) {
        val videosDataColumnIndex = videosCursor.getColumnIndex(MediaStore.MediaColumns.DATA)
        currentFolder = ""
        while (videosCursor.moveToNext()) {
            val videosData = videosCursor.getString(videosDataColumnIndex)
            val folderName = getFolderName(videosData)
            if (!currentFolder.equals(folderName, true)) {
                currentFolder = folderName
                directories.add(folderName)
            }
        }
    }

    imagesCursor?.close()
    videosCursor?.close()

    val sortedFolders = directories.sorted().map { folderName ->
        MediaFolder(folderName, uniqueId = folderName)
    }

    return sortedFolders
}

private fun getFolderName(fullPath: String): String {
    return fullPath.substringBeforeLast(SEPARATOR).takeLastWhile { it != SEPARATOR }
}

private fun skipElementIfNotValid(data: String, directoryName: String): Boolean {
    val folderName = getFolderName(data)
    val directory = data.substringAfterLast("$directoryName/")
    return !folderName.equals(directoryName, true) ||
        directory.startsWith(DOT) ||
        directory.contains(SEPARATOR)
}
