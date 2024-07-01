package com.attafitamim.file.picker.core.domain.interaction.file

import com.attafitamim.file.picker.core.domain.model.file.FileElementsResult
import com.attafitamim.file.picker.core.domain.model.file.FileFormat
import com.attafitamim.file.picker.core.domain.model.file.Volume
import com.attafitamim.file.picker.core.domain.repository.file.IFileRepository

class GetFiles(private val mediaRepository: IFileRepository) {

    suspend operator fun invoke(
        fullPath: String?,
        volume: Volume,
        fileFormats: List<FileFormat>?
    ): FileElementsResult = mediaRepository.loadFiles(
        fullPath,
        volume,
        fileFormats
    )
}
