package com.attafitamim.file.picker.core

import com.attafitamim.file.picker.core.domain.interaction.media.AddImageMedia
import com.attafitamim.file.picker.core.domain.interaction.media.AddMedia
import com.attafitamim.file.picker.core.domain.interaction.media.GetMediaElements
import com.attafitamim.file.picker.core.domain.interaction.media.GetMediaFolders
import com.attafitamim.file.picker.core.domain.repository.media.IMediaRepository

class FilePickerMediaUseCases(
    configuration: FilePickerConfiguration,
    factory: FilePickerRepositoryFactory = FilePickerRepositoryFactory(configuration),
    repository: IMediaRepository = factory.provideMediaRepository(),
) {

    val addImageMedia by lazy {
        AddImageMedia(repository)
    }

    val addMedia by lazy {
        AddMedia(repository)
    }

    val getMediaElements by lazy {
        GetMediaElements(repository)
    }

    val getMediaFolders by lazy {
        GetMediaFolders(repository)
    }
}