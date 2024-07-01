package com.attafitamim.file.picker.core.data.repository.file

import com.attafitamim.file.picker.core.data.source.file.IFileLocalSource
import com.attafitamim.file.picker.core.domain.model.file.FileElementsResult
import com.attafitamim.file.picker.core.domain.model.file.FileFormat
import com.attafitamim.file.picker.core.domain.model.file.Volume
import com.attafitamim.file.picker.core.domain.repository.file.IFileRepository

class FileRepository(private val localSource: IFileLocalSource) : IFileRepository {

    override suspend fun getVolumes(): List<Volume> =
        localSource.getVolumes()

    override suspend fun loadFiles(
        fullPath: String?,
        volume: Volume,
        fileFormats: List<FileFormat>?
    ): FileElementsResult = localSource.loadFiles(
        fullPath,
        volume,
        fileFormats
    )
}
