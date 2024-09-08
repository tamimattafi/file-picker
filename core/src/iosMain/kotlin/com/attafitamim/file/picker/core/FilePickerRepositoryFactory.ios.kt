package com.attafitamim.file.picker.core

import com.attafitamim.file.picker.core.data.local.MediaLocalSource
import com.attafitamim.file.picker.core.data.source.file.IFileLocalSource
import com.attafitamim.file.picker.core.data.source.media.IMediaLocalSource

actual fun getDefaultFileLocalSource(configuration: FilePickerConfiguration): IFileLocalSource {
    error("FileLocalSource is not available for ios")
}

actual fun getDefaultMediaLocalSource(configuration: FilePickerConfiguration): IMediaLocalSource =
    MediaLocalSource()
