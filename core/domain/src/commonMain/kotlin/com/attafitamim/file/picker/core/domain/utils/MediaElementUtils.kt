package com.attafitamim.file.picker.core.domain.utils

import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.utils.MIME_TYPE_IMAGE_JPEG
import com.attafitamim.file.picker.core.utils.MIME_TYPE_VIDEO_MP4

val MediaElement.defaultMimeType
    get() = when (this) {
        is MediaElement.ImageElement -> MIME_TYPE_IMAGE_JPEG
        is MediaElement.VideoElement -> MIME_TYPE_VIDEO_MP4
    }
