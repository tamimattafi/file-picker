package com.attafitamim.file.picker.core.domain.utils

import com.attafitamim.file.picker.core.domain.model.media.MediaResource

expect suspend fun MediaResource.getPath(): String?