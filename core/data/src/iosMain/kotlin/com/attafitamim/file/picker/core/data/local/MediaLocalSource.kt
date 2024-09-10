@file:OptIn(ExperimentalForeignApi::class)

package com.attafitamim.file.picker.core.data.local

import com.attafitamim.file.picker.core.data.source.media.IMediaLocalSource
import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder
import com.attafitamim.file.picker.core.domain.model.media.MediaLocation
import com.attafitamim.file.picker.core.domain.model.media.MediaResource
import com.attafitamim.file.picker.core.utils.MIME_TYPE_IMAGE_JPEG
import com.attafitamim.file.picker.core.utils.SECOND_IN_MILLIS
import com.attafitamim.file.picker.core.utils.async
import com.attafitamim.file.picker.core.utils.mimeType
import com.attafitamim.file.picker.core.utils.toNSData
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.random.nextULong
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSSortDescriptor
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLByDeletingLastPathComponent
import platform.Foundation.create
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.Photos.PHAsset
import platform.Photos.PHAssetCollection
import platform.Photos.PHAssetCollectionSubtypeAny
import platform.Photos.PHAssetCollectionTypeAlbum
import platform.Photos.PHAssetCollectionTypeSmartAlbum
import platform.Photos.PHAssetMediaTypeImage
import platform.Photos.PHAssetMediaTypeVideo
import platform.Photos.PHFetchOptions
import platform.Photos.PHFetchResult
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import platform.UIKit.UISaveVideoAtPathToSavedPhotosAlbum

private const val IMAGES_FOLDER = "/image"
private const val VIDEOS_FOLDER = "/video"
private const val IMAGE_PNG_FORMAT = ".png"
private const val IMAGE_JPEG_FORMAT = ".jpg"
private const val VIDEO_FORMAT_FORMAT = ".mp4"
private const val COMPRESSION_QUALITY = 1.0

class MediaLocalSource : IMediaLocalSource {

    override suspend fun getFolders(
        includeImages: Boolean,
        includeVideos: Boolean
    ): List<MediaFolder> = runCatching {
        val mediaCollections = getMediaCollections(includeImages, includeVideos)

        mediaCollections.map { collection ->
            val uniqueId = collection.localIdentifier
            val name = collection.localizedTitle.orEmpty()

            MediaFolder(name, uniqueId)
        }.sortedBy(MediaFolder::name)
    }.getOrDefault(emptyList())

    override suspend fun getMediaElements(
        includeImages: Boolean,
        includeVideos: Boolean,
        descendingDirection: Boolean,
        mediaFolder: MediaFolder?,
        expectedSize: Int
    ): List<MediaElement> {
        val urlsAsync = mutableListOf<MediaElement?>()
        val options = PHFetchOptions.new()?.apply {
            this.sortDescriptors = listOf(
                NSSortDescriptor(DATE_SORT_KEY, ascending = !descendingDirection)
            )
        }

        val assets = if (mediaFolder != null) {
            val collection = getMediaCollections(includeImages, includeVideos)
                .first { collection -> collection.localIdentifier == mediaFolder.uniqueId }
            PHAsset.fetchAssetsInAssetCollection(collection, options)
        } else {
            PHAsset.fetchAssetsWithOptions(options)
        }.toMediaElements(includeImages, includeVideos, expectedSize)

        assets.forEach { asset ->
            val media = asset.toMediaElement()
            urlsAsync.add(media)
        }

        return urlsAsync.filterNotNull()
    }

    override suspend fun addImage(
        imageBytes: ByteArray,
        title: String,
        currentTime: Long,
        mimeType: String,
        description: String?,
        isDateEnabled: Boolean
    ): MediaLocation {
        val imagePath = saveUIImageAndGetPathWithFormat(imageBytes, mimeType)
        return MediaLocation(imagePath, mimeType)
    }

    override suspend fun addMedia(
        path: String,
        mimeType: String,
        title: String,
        currentTime: Long,
        description: String?,
        isDateEnabled: Boolean,
        isPhoto: Boolean
    ): MediaLocation {
        val mediaPath = insertMediaToAlbum(path, isPhoto)
        return MediaLocation(mediaPath, mimeType)
    }

    private fun PHAsset.toMediaElement(): MediaElement? = when (mediaType) {
        PHAssetMediaTypeImage -> toImageElement()
        PHAssetMediaTypeVideo -> toVideoElement()
        else -> null
    }

    private fun PHAsset.toImageElement(): MediaElement.ImageElement {
        val value = MediaResource(value = MediaResource.Value.Asset(asset = this))
        val date = modificationDate?.timeIntervalSince1970?.roundToInt() ?: 0
        return MediaElement.ImageElement(
            value,
            mimeType,
            date
        )
    }

    private fun PHAsset.toVideoElement(): MediaElement.VideoElement {
        val resource = MediaResource(value = MediaResource.Value.Asset(asset = this))
        val date = modificationDate?.timeIntervalSince1970?.roundToInt() ?: 0
        val durationInMillis = duration.toLong() * SECOND_IN_MILLIS
        return MediaElement.VideoElement(
            resource,
            mimeType,
            date,
            durationInMillis
        )
    }

    private suspend fun getMediaCollections(
        includeImages: Boolean,
        includeVideos: Boolean
    ): List<PHAssetCollection> {
        val albumResultAsync = async {
            PHAssetCollection.fetchAssetCollectionsWithType(
                type = PHAssetCollectionTypeAlbum,
                subtype = PHAssetCollectionSubtypeAny,
                options = null
            ).toMediaFolders(includeImages, includeVideos)
        }

        val smartAlbumResultAsync = async {
            PHAssetCollection.fetchAssetCollectionsWithType(
                type = PHAssetCollectionTypeSmartAlbum,
                subtype = PHAssetCollectionSubtypeAny,
                options = null
            ).toMediaFolders(includeImages, includeVideos)
        }

        return (albumResultAsync.await() + smartAlbumResultAsync.await())
            .distinctBy { collection -> collection.localIdentifier }
    }

    private fun PHFetchResult.toMediaFolders(
        includeImages: Boolean,
        includeVideos: Boolean
    ): List<PHAssetCollection> = List(count.toInt()) { index ->
        val collection = this.objectAtIndex(index.toULong()) as? PHAssetCollection
        collection?.takeIf { collection.isValid(includeImages, includeVideos) }
    }.filterNotNull()

    private fun PHAssetCollection.isValid(
        includeImages: Boolean,
        includeVideos: Boolean
    ): Boolean {
        val assets = PHAsset.fetchAssetsInAssetCollection(this, options = null)

        return assets.toMediaElements(includeImages, includeVideos).isNotEmpty()
    }

    // TODO optimize mapping
    private fun PHFetchResult.toMediaElements(
        includeImages: Boolean,
        includeVideos: Boolean,
        maxSize: Int = Int.MAX_VALUE
    ): List<PHAsset> = List(count.toInt().coerceAtMost(maxSize)) { index ->
        this.objectAtIndex(index.toULong()) as? PHAsset
    }.mapNotNull { phAsset ->
        phAsset?.takeIf { asset ->
            asset.isImage(includeImages) || asset.isVideo(includeVideos)
        }
    }

    // TODO optimize media types filter on fetching data from os
    private fun PHAsset.isVideo(includeVideos: Boolean) =
        includeVideos && mediaType == PHAssetMediaTypeVideo

    private fun PHAsset.isImage(includeImages: Boolean) =
        includeImages && mediaType == PHAssetMediaTypeImage

    private companion object {

        const val DATE_SORT_KEY = "creationDate"
    }
}

private fun saveUIImageAndGetPathWithFormat(imageBitmap: ByteArray, mimeType: String): String =
    saveUIImageAndGetPathWithFormat(imageBitmap.toNSData(), mimeType)

@OptIn(ExperimentalForeignApi::class)
private fun saveUIImageAndGetPathWithFormat(
    imageData: NSData,
    mimeType: String
): String {
    val imagePathWithFormat = createMediaPathWithFormat(isPhoto = true, mimeType)
    UIImage.imageWithData(imageData)?.let { uIImage ->
        UIImageWriteToSavedPhotosAlbum(
            uIImage,
            null,
            null,
            null
        )
    }

    return saveImageToCustomPath(imageData, imagePathWithFormat)
}

@OptIn(ExperimentalForeignApi::class)
private fun saveImageToCustomPath(data: NSData, filePath: String): String {
    val directoryUrl = NSURL.fileURLWithPath(filePath).URLByDeletingLastPathComponent
    if (!NSFileManager.defaultManager.fileExistsAtPath(directoryUrl?.path.orEmpty()) &&
        directoryUrl != null
    ) {
        NSFileManager.defaultManager.createDirectoryAtURL(
            directoryUrl, withIntermediateDirectories = true, attributes = null, error = null
        )
    }

    data.writeToFile(filePath, atomically = true)
    return filePath
}

private fun insertMediaToAlbum(
    mediaPath: String,
    isPhoto: Boolean
): String = if (isPhoto) {
    insertImageToAlbum(mediaPath)
} else {
    insertVideoToAlbum(mediaPath)
}

private fun insertImageToAlbum(path: String): String {
    val nsPath = NSURL.fileURLWithPath(path, true).path
    val image = UIImage.imageWithContentsOfFile(requireNotNull(nsPath))
    val jpg = UIImageJPEGRepresentation(
        requireNotNull(image),
        COMPRESSION_QUALITY
    )

    return saveUIImageAndGetPathWithFormat(requireNotNull(jpg), MIME_TYPE_IMAGE_JPEG)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun insertVideoToAlbum(path: String): String {
    val videoPath = createMediaPathWithFormat(isPhoto = false)
    val data = NSData.create(NSURL.fileURLWithPath(path, true))
    val fileUrl = NSURL.fileURLWithPath(videoPath)
    val directoryUrl = fileUrl.URLByDeletingLastPathComponent
    if (!NSFileManager.defaultManager.fileExistsAtPath(directoryUrl?.path.orEmpty()) &&
        directoryUrl != null
    ) {
        NSFileManager.defaultManager.createDirectoryAtURL(
            directoryUrl, withIntermediateDirectories = true, attributes = null, error = null
        )
    }
    data?.writeToFile(videoPath, atomically = true)
    UISaveVideoAtPathToSavedPhotosAlbum(path, null, null, null)
    return videoPath
}

private fun createMediaPathWithFormat(isPhoto: Boolean, mimeType: String? = null): String {
    val cacheDir = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as NSString
    return if (isPhoto) {
        val imageDirectory = cacheDir.stringByAppendingPathComponent(IMAGES_FOLDER)
        if (mimeType == MIME_TYPE_IMAGE_JPEG) {
            imageDirectory + randomDirectoryId() + IMAGE_JPEG_FORMAT
        } else {
            imageDirectory + randomDirectoryId() + IMAGE_PNG_FORMAT
        }
    } else {
        val videoDirectory = cacheDir.stringByAppendingPathComponent(VIDEOS_FOLDER)
        videoDirectory + randomDirectoryId() + VIDEO_FORMAT_FORMAT
    }
}

private fun randomDirectoryId() = Random.nextULong().toString()