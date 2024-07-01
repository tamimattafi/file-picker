package com.attafitamim.file.picker.core.domain.model.file

enum class FileFormat(
    val extensions: List<String>,
    val mimetypes: List<String>
) {
    JPG(
        extensions = listOf("jpg", "jpeg"),
        mimetypes = listOf("image/jpg", "image/jpeg")
    ),

    PNG(
        extensions = listOf("png"),
        mimetypes = listOf("image/png")
    ),

    SVG(
        extensions = listOf("svg"),
        mimetypes = listOf("image/svg+xml")
    ),

    PDF(
        extensions = listOf("pdf"),
        mimetypes = listOf("application/pdf")
    ),

    MP4(
        extensions = listOf("mp4"),
        mimetypes = listOf("video/mp4")
    ),

    MS_WORD(
        extensions = listOf("doc", "docx"),
        mimetypes = listOf(
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        )
    ),

    EXCEL(
        extensions = listOf("xls", "xlsx"),
        mimetypes = listOf("application/vnd.ms-excel")
    )
}
