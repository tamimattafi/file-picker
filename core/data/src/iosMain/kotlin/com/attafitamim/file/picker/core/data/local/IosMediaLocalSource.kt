@file:OptIn(BetaInteropApi::class)

package com.attafitamim.file.picker.core.data.local

import com.attafitamim.file.picker.core.data.source.media.ILocalSourceMediaRetriever
import com.attafitamim.file.picker.core.data.source.media.IMediaLocalSource
import com.attafitamim.file.picker.core.data.source.media.IosMediaElementImage
import com.attafitamim.file.picker.core.data.source.media.IosMediaElementVideo
import com.attafitamim.file.picker.core.data.source.media.MediaAsset
import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder
import com.attafitamim.file.picker.core.utils.MIME_TYPE_IMAGE_JPEG
import com.attafitamim.file.picker.core.utils.MimeTypeHelper
import com.attafitamim.file.picker.core.utils.SECOND_IN_MILLIS
import com.attafitamim.file.picker.core.utils.async
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSSortDescriptor
import platform.Photos.PHAsset
import platform.Photos.PHAssetCollection
import platform.Photos.PHAssetCollectionSubtypeAny
import platform.Photos.PHAssetCollectionTypeAlbum
import platform.Photos.PHAssetCollectionTypeSmartAlbum
import platform.Photos.PHAssetMediaTypeImage
import platform.Photos.PHAssetMediaTypeVideo
import platform.Photos.PHFetchOptions
import platform.Photos.PHFetchResult
import kotlin.coroutines.resume
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.random.nextULong
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.URLByDeletingLastPathComponent
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.stringByAppendingPathComponent
import platform.Foundation.writeToFile
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImageWriteToSavedPhotosAlbum
import platform.UIKit.UISaveVideoAtPathToSavedPhotosAlbum
import platform.posix.memcpy

private const val IMAGES_FOLDER = "/image"
private const val VIDEOS_FOLDER = "/video"
private const val IMAGE_PNG_FORMAT = ".png"
private const val IMAGE_JPEG_FORMAT = ".jpg"
private const val VIDEO_FORMAT_FORMAT = ".mp4"
private const val COMPRESSION_QUALITY = 1.0

class IosMediaLocalSource(
    private val mediaRetriever: ILocalSourceMediaRetriever
) : IMediaLocalSource {

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
        val urlsAsync = mutableListOf<Deferred<MediaElement?>>()
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

        coroutineScope {
            withContext(Dispatchers.IO) {
                assets.forEach { asset ->
                    val media = async { asset.toMediaElement() }
                    urlsAsync.add(media)
                }
            }
        }
        return urlsAsync.awaitAll().filterNotNull()
    }

    override suspend fun addImage(
        imageBytes: ByteArray,
        title: String,
        currentTime: Long,
        mimeType: String,
        description: String?,
        isDateEnabled: Boolean
    ): MediaElement {
        val imagePath = saveUIImageAndGetPathWithFormat(imageBytes, mimeType)
        val timeInSeconds = (currentTime / SECOND_IN_MILLIS).toInt()
        return MediaElement.ImageElement(imagePath, mimeType, timeInSeconds)
    }

    override suspend fun addMedia(
        path: String,
        mimeType: String,
        title: String,
        currentTime: Long,
        description: String?,
        isDateEnabled: Boolean,
        isPhoto: Boolean
    ): MediaElement {
        val mediaPath = insertMediaToAlbum(path, isPhoto)
        val timeInSeconds = (currentTime / SECOND_IN_MILLIS).toInt()
        return MediaElement.ImageElement(mediaPath, mimeType, timeInSeconds)
    }

    private suspend fun PHAsset.toMediaElement(): MediaElement? =
        suspendCancellableCoroutine { continuation ->
            mediaRetriever.handleInput(phAsset = MediaAsset(this)) { iosMediaElement ->
                val element = when (iosMediaElement) {
                    is IosMediaElementImage -> iosMediaElement.toImage()
                    is IosMediaElementVideo -> iosMediaElement.toVideo()
                    null -> null
                }

                continuation.resume(element)
            }
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

    private fun IosMediaElementImage.toImage(): MediaElement.ImageElement? {
        val path = this.path ?: return null

        val mimeType = getMimeType(path)
        val dateInSeconds = date?.roundToInt() ?: 0

        return MediaElement.ImageElement(
            path,
            mimeType.orEmpty(),
            dateInSeconds
        )
    }

    private fun getMimeType(path: String): String? {
        val extension = path.takeLastWhile { it != '.' }
        return MimeTypeHelper.guessMimeTypeFromExtension(extension)
    }

    private fun IosMediaElementVideo.toVideo(): MediaElement.VideoElement? {
        val path = this.path ?: return null

        val mimeType = getMimeType(path)
        val dateInSeconds = date?.roundToInt() ?: 0
        val duration = duration.toLong() * SECOND_IN_MILLIS

        return MediaElement.VideoElement(
            path,
            mimeType.orEmpty(),
            dateInSeconds,
            duration
        )
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


@OptIn(ExperimentalForeignApi::class)
private fun saveUIImageAndGetPathWithFormat(imageBitmap: ByteArray, mimeType: String): String {
    val imagePathWithFormat = createMediaPathWithFormat(isPhoto = true, mimeType)
    val imageData = imageBitmap.usePinned { byte ->
        NSData.dataWithBytes(byte.addressOf(0), imageBitmap.size.toULong())
    }
    UIImage.imageWithData(imageData)?.let { uIImage ->
        UIImageWriteToSavedPhotosAlbum(uIImage, null, null, null)
    }
    return saveImageToCustomPath(imageBitmap, imagePathWithFormat)
}

@OptIn(ExperimentalForeignApi::class)
private fun saveImageToCustomPath(imageData: ByteArray, filePath: String): String {
    val data = imageData.usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), imageData.size.toULong())
    }

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

@OptIn(ExperimentalForeignApi::class)
private fun insertImageToAlbum(path: String): String {
    var localMediaPath: String? = null
    val data = NSData.dataWithContentsOfURL(NSURL.fileURLWithPath(path, true))
    val image = UIImage.imageWithData(requireNotNull(data))
    UIImageJPEGRepresentation(requireNotNull(image), COMPRESSION_QUALITY)?.let { jpg ->
        val byteArray = ByteArray(jpg.length.toInt()).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), jpg.bytes, jpg.length)
            }
        }
        localMediaPath = saveUIImageAndGetPathWithFormat(byteArray, MIME_TYPE_IMAGE_JPEG)
    }
    return requireNotNull(localMediaPath)
}

@OptIn(ExperimentalForeignApi::class)
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