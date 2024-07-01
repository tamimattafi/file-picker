package com.attafitamim.file.picker.core.data.local

import com.attafitamim.file.picker.core.data.source.media.ILocalSourceMediaRetriever
import com.attafitamim.file.picker.core.data.source.media.IMediaLocalSource
import com.attafitamim.file.picker.core.data.source.media.IosMediaElement
import com.attafitamim.file.picker.core.data.source.media.MediaAsset
import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaFolder
import com.attafitamim.file.picker.utils.MimeTypeHelper
import com.attafitamim.file.picker.utils.SECOND_IN_MILLIS
import com.attafitamim.file.picker.utils.async
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

    private suspend fun PHAsset.toMediaElement(): MediaElement? =
        suspendCancellableCoroutine { continuation ->
            mediaRetriever.handleInput(phAsset = MediaAsset(this)) { iosMediaElement ->
                val element = when (iosMediaElement) {
                    is IosMediaElement.Image -> iosMediaElement.toImage()
                    is IosMediaElement.Video -> iosMediaElement.toVideo()
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

    private fun IosMediaElement.Image.toImage(): MediaElement.ImageElement? {
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

    private fun IosMediaElement.Video.toVideo(): MediaElement.VideoElement? {
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
