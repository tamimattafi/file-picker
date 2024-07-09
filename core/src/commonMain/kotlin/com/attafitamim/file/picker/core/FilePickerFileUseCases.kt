package com.attafitamim.file.picker.core

import com.attafitamim.file.picker.core.domain.interaction.file.GetFileVolumes
import com.attafitamim.file.picker.core.domain.interaction.file.GetFiles
import com.attafitamim.file.picker.core.domain.repository.file.IFileRepository

class FilePickerFileUseCases(
    configuration: FilePickerConfiguration,
    factory: FilePickerRepositoryFactory = FilePickerRepositoryFactory(configuration),
    repository: IFileRepository? = null,
) {

    private val fileRepository by lazy {
        repository ?: factory.provideFileRepository()
    }

    val getFiles by lazy {
        GetFiles(fileRepository)
    }

    val getFileVolumes by lazy {
        GetFileVolumes(fileRepository)
    }
}