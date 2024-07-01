package com.attafitamim.file.picker.core.data.source.file

import com.attafitamim.file.picker.core.domain.model.file.FileElementsResult
import com.attafitamim.file.picker.core.domain.model.file.FileFormat
import com.attafitamim.file.picker.core.domain.model.file.Volume

interface IFileLocalSource {

    suspend fun getVolumes(): List<Volume>

    suspend fun loadFiles(
        fullPath: String?,
        volume: Volume,
        fileFormats: List<FileFormat>?,
    ): FileElementsResult
}
