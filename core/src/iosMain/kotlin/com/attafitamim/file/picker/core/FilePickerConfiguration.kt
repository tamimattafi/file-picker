package com.attafitamim.file.picker.core

import com.attafitamim.file.picker.core.data.source.media.ILocalSourceMediaRetriever

actual class FilePickerConfiguration(
    val mediaRetriever: ILocalSourceMediaRetriever
)
