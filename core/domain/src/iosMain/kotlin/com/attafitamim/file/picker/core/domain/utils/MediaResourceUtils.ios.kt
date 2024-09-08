package com.attafitamim.file.picker.core.domain.utils

import com.attafitamim.file.picker.core.domain.model.media.MediaResource
import com.attafitamim.file.picker.core.utils.getPath

actual suspend fun MediaResource.getPath(): String? = when (value) {
    is MediaResource.Value.Asset -> value.asset.getPath()
    is MediaResource.Value.Path -> value.uri
}
