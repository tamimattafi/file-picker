package com.attafitamim.file.picker.core.domain.model.file

data class FileElementsResult(
    val fileElements: List<FileElement>,
    val parentPath: String?
)
