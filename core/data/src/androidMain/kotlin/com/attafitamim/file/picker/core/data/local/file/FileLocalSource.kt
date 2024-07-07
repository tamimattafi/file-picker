package com.attafitamim.file.picker.core.data.local.file

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer.MetricsConstants.MIME_TYPE_VIDEO
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.webkit.MimeTypeMap
import com.attafitamim.file.picker.core.data.source.file.IFileLocalSource
import com.attafitamim.file.picker.core.domain.model.file.FileElement
import com.attafitamim.file.picker.core.domain.model.file.FileElementsResult
import com.attafitamim.file.picker.core.domain.model.file.FileFormat
import com.attafitamim.file.picker.core.domain.model.file.Volume
import com.attafitamim.file.picker.core.utils.MIME_TYPE_IMAGE
import com.attafitamim.file.picker.core.utils.MIME_TYPE_TEXT_PLAIN
import com.attafitamim.file.picker.core.utils.isSdk30AndHigher
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loader only for api below 30
 */
class FileLocalSource(
    private val context: Context
) : IFileLocalSource {

    override suspend fun getVolumes(): List<Volume> = withContext(Dispatchers.IO) {
        requireSdkVersion()
        val storageVolumes = getStorageVolumes()

        return@withContext storageVolumes.toVolumes()
    }

    override suspend fun loadFiles(
        fullPath: String?,
        volume: Volume,
        fileFormats: List<FileFormat>?
    ): FileElementsResult = withContext(Dispatchers.IO) {
        requireSdkVersion()

        val volumeRootFile = getRootFileForVolume(volume)
        val rootFile = if (fullPath != null) File(fullPath) else volumeRootFile
        val extensionsFilter = fileFormats?.flatMap(FileFormat::extensions)

        val listOfFiles = rootFile.listFiles()?.filterNot(File::isHidden)
            ?.filter { it.isCorrectExtension(extensionsFilter) }
            ?.sortedByDefaultComparator()

        val fileElements = listOfFiles?.toFileElements().orEmpty()
        val canOpenParent = volumeRootFile != rootFile
        val parentFullPath = if (canOpenParent) {
            rootFile.parentFile?.absolutePath
        } else {
            null
        }

        return@withContext FileElementsResult(
            fileElements,
            parentFullPath
        )
    }

    private fun requireSdkVersion() {
        require(!isSdk30AndHigher)
    }

    private fun getStorageVolumes(): List<StorageVolume> {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        return storageManager.storageVolumes
            .filter { it.state.isMounted() }
            .sortedBy(StorageVolume::isRemovable)
    }

    private fun getRootFileForVolume(
        volume: Volume
    ): File {
        val storageVolumes = getStorageVolumes()
        val correctVolume = storageVolumes.getCorrectVolume(volume)

        return correctVolume.getFileWithReflection()
    }

    private fun getFilesComparator() = Comparator<File> { firstFile, secondFile ->
        when {
            firstFile.isDirectory && !secondFile.isDirectory -> 1
            firstFile.isDirectory && secondFile.isDirectory ->
                if (firstFile.lastModified() >= secondFile.lastModified()) 1 else -1
            secondFile.isDirectory && !firstFile.isDirectory -> -1
            else -> 0
        }
    }

    private fun List<File>.toFileElements(): List<FileElement> = this.map { file ->
        val name = file.name
        val fullPath = file.absolutePath
        val mimeType = file.extension.let(::getMimeTypeFromExtension) ?: MIME_TYPE_TEXT_PLAIN

        when {
            file.isDirectory -> fileToFolder(name, fullPath)
            mimeType.startsWith(MIME_TYPE_IMAGE) -> file.toImage(name, fullPath, mimeType)
            mimeType.startsWith(MIME_TYPE_VIDEO) -> file.toVideo(name, fullPath, mimeType)
            else -> file.toFile(name, fullPath, mimeType)
        }
    }

    private fun File.toFile(
        name: String,
        fullPath: String,
        mimeType: String
    ) = FileElement.File(
        name,
        fullPath,
        size = this.length(),
        lastModified = this.lastModified(),
        mimeType = mimeType
    )

    private fun File.toVideo(
        name: String,
        fullPath: String,
        mimeType: String
    ) = FileElement.Video(
        name,
        fullPath,
        size = this.length(),
        lastModified = this.lastModified(),
        mimeType = mimeType
    )

    private fun fileToFolder(name: String, fullPath: String) = FileElement.Folder(
        name,
        fullPath
    )

    private fun File.toImage(
        name: String,
        fullPath: String,
        mimeType: String
    ) = FileElement.Image(
        name,
        fullPath,
        size = this.length(),
        lastModified = this.lastModified(),
        mimeType = mimeType
    )

    private fun getMimeTypeFromExtension(extension: String) =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)

    private fun List<File>.sortedByDefaultComparator(): List<File> {
        val filesComparator = getFilesComparator()

        return this.sortedWith(filesComparator).reversed()
    }

    private fun File.isCorrectExtension(extensionFilter: List<String>?): Boolean {
        if (extensionFilter == null) return true

        return this.isDirectory || this.extension in extensionFilter
    }

    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun StorageVolume.getFileWithReflection(): File {
        val privateField = this.javaClass.getDeclaredField(VARIABLE_NAME_FOR_REFLECTION)
        privateField.isAccessible = true

        return privateField.get(this) as File
    }

    private fun List<StorageVolume>.toVolumes() = this.mapIndexed { index, storageVolume ->
        val uuid = storageVolume.uuid

        Volume(
            number = index,
            uuid = uuid
        )
    }

    private fun List<StorageVolume>.getCorrectVolume(volume: Volume): StorageVolume =
        this.find { storageVolume ->
            storageVolume.uuid != null && storageVolume.uuid == volume.uuid
        } ?: this.elementAtOrElse(volume.number) {
            this.first()
        }

    private fun String.isMounted() = this == Environment.MEDIA_MOUNTED ||
        this == Environment.MEDIA_MOUNTED_READ_ONLY

    private companion object {

        const val VARIABLE_NAME_FOR_REFLECTION = "mPath"
    }
}
