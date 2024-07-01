package com.attafitamim.file.picker.core.domain.model.file

sealed interface FileElement {
    val name: String
    val fullPath: String

    data class Folder(
        override val name: String,
        override val fullPath: String
    ) : FileElement

    data class Image(
        override val name: String,
        override val fullPath: String,
        val size: Long,
        val lastModified: Long,
        val mimeType: String
    ) : FileElement

    data class Video(
        override val name: String,
        override val fullPath: String,
        val size: Long,
        val lastModified: Long,
        val mimeType: String
    ) : FileElement

    data class File(
        override val name: String,
        override val fullPath: String,
        val size: Long,
        val lastModified: Long,
        val mimeType: String
    ) : FileElement
}
