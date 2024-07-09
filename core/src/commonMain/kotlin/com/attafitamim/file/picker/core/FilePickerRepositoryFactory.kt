package com.attafitamim.file.picker.core

import com.attafitamim.file.picker.core.data.repository.file.FileRepository
import com.attafitamim.file.picker.core.data.repository.media.MediaRepository
import com.attafitamim.file.picker.core.data.source.file.IFileLocalSource
import com.attafitamim.file.picker.core.data.source.media.IMediaLocalSource
import com.attafitamim.file.picker.core.domain.repository.file.IFileRepository
import com.attafitamim.file.picker.core.domain.repository.media.IMediaRepository

expect fun getDefaultFileLocalSource(configuration: FilePickerConfiguration): IFileLocalSource
expect fun getDefaultMediaLocalSource(configuration: FilePickerConfiguration): IMediaLocalSource

class FilePickerRepositoryFactory(
    private val configuration: FilePickerConfiguration
) {

    fun provideFileRepository(
        localSource: IFileLocalSource = getDefaultFileLocalSource(configuration)
    ): IFileRepository = FileRepository(localSource)

    fun provideMediaRepository(
        localSource: IMediaLocalSource = getDefaultMediaLocalSource(configuration)
    ): IMediaRepository = MediaRepository(localSource)
}
