package com.attafitamim.file.picker.core

import com.attafitamim.file.picker.core.data.local.file.FileLocalSource
import com.attafitamim.file.picker.core.data.local.media.MediaLocalSource
import com.attafitamim.file.picker.core.data.local.media.oldapi.MediaLocalSourceLegacy
import com.attafitamim.file.picker.core.data.source.file.IFileLocalSource
import com.attafitamim.file.picker.core.data.source.media.IMediaLocalSource
import com.attafitamim.file.picker.core.utils.isSdk30AndHigher

actual fun getDefaultFileLocalSource(
    configuration: FilePickerConfiguration
): IFileLocalSource = FileLocalSource(configuration.context)

actual fun getDefaultMediaLocalSource(configuration: FilePickerConfiguration): IMediaLocalSource =
    if (isSdk30AndHigher) {
        MediaLocalSource(configuration.context, configuration.appFolder)
    } else {
        MediaLocalSourceLegacy(configuration.context, configuration.appFolder)
    }