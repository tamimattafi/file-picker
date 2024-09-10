package com.attafitamim.file.picker.core.utils

const val MIME_TYPE_SEPARATOR = "/"
const val EXTENSION_SEPARATOR = "."
const val NAME_SEPARATOR = "_"

const val EXTENSION_PLAIN_TEXT = "txt"
const val EXTENSION_JPEG = "jpg"
const val EXTENSION_WAV = "wav"
const val EXTENSION_MP3 = "mp3"
const val EXTENSION_HEIC = "heic"
const val EXTENSION_HEIF = "heif"

const val MIME_TYPE_UNKNOWN = "unknown/"
const val MIME_TYPE_VIDEO = "video/"
const val MIME_TYPE_AUDIO = "audio/"
const val MIME_TYPE_APPLICATION = "application/"
const val MIME_TYPE_TEXT = "text/"
const val MIME_TYPE_IMAGE = "image/"

const val MIME_TYPE_IMAGE_JPEG = "image/jpeg"
const val MIME_TYPE_IMAGE_PNG = "image/png"
const val MIME_TYPE_PDF = "application/pdf"
const val MIME_TYPE_ZIP = "application/zip"
const val MIME_TYPE_XD = "application/xd"
const val MIME_TYPE_AUDIO_MP4 = "audio/mp4"
const val MIME_TYPE_VIDEO_MP4 = "video/mp4"
const val MIME_TYPE_TEXT_PLAIN = "text/plain"
const val MIME_TYPE_AUDIO_MP3 = "audio/mpeg"
const val MIME_TYPE_AUDIO_XWAV = "audio/x-wav"
const val MIME_TYPE_AUDIO_VND_WAVE = "audio/vnd.wave"
const val MIME_TYPE_HTML = "text/html"

const val MIME_TYPE_WORD_1 = "application/msword"
const val MIME_TYPE_WORD_2 =
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

const val MIME_TYPE_EXCEL_1 = "application/vnd.ms-excel.sheet.binary.macroEnabled.12"
const val MIME_TYPE_EXCEL_2 = "application/vnd.ms-excel"
const val MIME_TYPE_EXCEL_3 = "application/vnd.ms-excel.sheet.macroEnabled.12"

const val MIME_TYPE_PSD_1 = "image/vnd.adobe.photoshop"
const val MIME_TYPE_PSD_2 = "application/x-photoshop"
const val MIME_TYPE_PSD_3 = "application/photoshop"
const val MIME_TYPE_PSD_4 = "application/psd"
const val MIME_TYPE_PSD_5 = "image/psd"

const val MIME_TYPE_AI_1 = "application/postscript"
const val MIME_TYPE_AI_2 = "application/illustrator"

fun getFallbackExtensionFromMimeType(mimeType: String): String = when (mimeType) {
    MIME_TYPE_IMAGE_JPEG -> EXTENSION_JPEG
    MIME_TYPE_TEXT_PLAIN -> EXTENSION_PLAIN_TEXT
    MIME_TYPE_AUDIO_XWAV,
    MIME_TYPE_AUDIO_VND_WAVE -> EXTENSION_WAV
    MIME_TYPE_AUDIO_MP3 -> EXTENSION_MP3
    else -> mimeType.substringAfterLast(MIME_TYPE_SEPARATOR)
}

fun isVideo(mimeType: String): Boolean {
    return mimeType.startsWith(MIME_TYPE_VIDEO)
}

fun isAudio(mimeType: String): Boolean {
    return mimeType.startsWith(MIME_TYPE_AUDIO)
}

fun getMimeType(path: String): String? {
    val extension = path.substringAfterLast(".")
    return MimeTypeHelper.guessMimeTypeFromExtension(extension)
}
