package com.attafitamim.file.picker.core.domain.interaction.file

import com.attafitamim.file.picker.core.domain.model.file.Volume
import com.attafitamim.file.picker.core.domain.repository.file.IFileRepository

class GetFileVolumes(private val fileRepository: IFileRepository) {

    suspend operator fun invoke(limit: Int = MAX_SUPPORTED_VOLUMES): List<Volume> =
        fileRepository.getVolumes().take(limit)

    private companion object {
        const val MAX_SUPPORTED_VOLUMES = 2
    }
}
