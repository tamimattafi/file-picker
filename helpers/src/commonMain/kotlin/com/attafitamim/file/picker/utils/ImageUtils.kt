package com.attafitamim.file.picker.utils

import androidx.compose.ui.graphics.ImageBitmap
import com.attafitamim.file.picker.core.utils.EXTENSION_HEIC
import com.attafitamim.file.picker.core.utils.EXTENSION_HEIF
import com.attafitamim.file.picker.core.utils.EXTENSION_SEPARATOR

expect fun ByteArray.toImageBitmap(): ImageBitmap?

fun String.isHEICUri(): Boolean {
    val extension = substringAfterLast(EXTENSION_SEPARATOR)
    return extension.equals(EXTENSION_HEIC, ignoreCase = true) ||
            equals(EXTENSION_HEIF, ignoreCase = true)
}
