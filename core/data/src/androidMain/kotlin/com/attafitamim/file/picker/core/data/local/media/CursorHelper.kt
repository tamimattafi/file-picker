@file:RequiresApi(Build.VERSION_CODES.R)

package com.attafitamim.file.picker.core.data.local.media

import android.content.ContentUris
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.database.getStringOrNull
import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder

private const val SEPARATOR = '/'
private const val DOT = '.'
private const val VIDEO_TYPE = "video"

fun getMediaElementsForNewApi(
    mediaCursor: Cursor,
    mediaFolder: MediaFolder?,
    expectedSize: Int
): List<MediaElement> {
    val idColumnIndex = mediaCursor.getColumnIndex(MediaStore.MediaColumns._ID)
    val modifiedDateColumnIndex =
        mediaCursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
    val mimeColumnIndex =
        mediaCursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
    val durationColumnIndex =
        mediaCursor.getColumnIndex(MediaStore.MediaColumns.DURATION)
    val relativePathColumnIndex =
        mediaCursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)

    val mediaElements = ArrayList<MediaElement>(expectedSize)
    while (mediaElements.size <= expectedSize && mediaCursor.moveToNext()) {
        if (mediaFolder != null) {
            val relativePath = mediaCursor.getString(relativePathColumnIndex)
            if (skipElementIfNotValid(relativePath, mediaFolder.name)) {
                continue
            }
        }

        val id = mediaCursor.getString(idColumnIndex).toLong()
        val dateModifiedInSeconds = mediaCursor.getInt(modifiedDateColumnIndex)
        val mimeType = mediaCursor.getString(mimeColumnIndex)

        val isVideoType = mimeType.contains(VIDEO_TYPE)

        val mediaElement = if (isVideoType) {
            val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                .toString()

            val durationMilliseconds = mediaCursor.getStringOrNull(durationColumnIndex)?.toLong()

            MediaElement.VideoElement(
                uri,
                mimeType,
                dateModifiedInSeconds,
                durationMilliseconds
            )
        } else {
            val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                .toString()

            MediaElement.ImageElement(
                uri,
                mimeType,
                dateModifiedInSeconds
            )
        }

        mediaElements.add(mediaElement)
    }

    mediaCursor.close()

    return mediaElements
}

fun getMediaFoldersForNewApi(mediaCursor: Cursor): List<MediaFolder> {
    val directories = HashSet<String>()
    val mediaRelativePathColumnIndex =
        mediaCursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)

    var currentFolder = ""
    while (mediaCursor.moveToNext()) {
        val relativePath = mediaCursor.getString(mediaRelativePathColumnIndex)
        val folderName = getFolderName(relativePath)

        if (!currentFolder.equals(folderName, true)) {
            currentFolder = folderName
            directories.add(folderName)
        }
    }

    mediaCursor.close()

    val sortedFolders = directories.sorted().map { folderName ->
        MediaFolder(folderName, uniqueId = folderName)
    }

    return sortedFolders
}

private fun getFolderName(fullPath: String): String {
    return fullPath.substringBeforeLast(SEPARATOR).takeLastWhile { it != SEPARATOR }
}

private fun skipElementIfNotValid(relativePath: String, directoryName: String): Boolean {
    val folderName = getFolderName(relativePath)
    val directory = relativePath.substringAfterLast("$directoryName/")

    return !folderName.equals(directoryName, true) ||
            directory.startsWith(DOT) ||
            directory.contains(SEPARATOR)
}
