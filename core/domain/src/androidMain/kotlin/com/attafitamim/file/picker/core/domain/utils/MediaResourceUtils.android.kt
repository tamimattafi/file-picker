package com.attafitamim.file.picker.core.domain.utils

import com.attafitamim.file.picker.core.domain.model.media.MediaResource

actual suspend fun MediaResource.getPath(): String? = uri.toString()