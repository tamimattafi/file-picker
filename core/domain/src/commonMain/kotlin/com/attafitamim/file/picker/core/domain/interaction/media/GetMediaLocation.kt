package com.attafitamim.file.picker.core.domain.interaction.media

import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaLocation
import com.attafitamim.file.picker.core.domain.utils.defaultMimeType
import com.attafitamim.file.picker.core.domain.utils.getPath
import com.attafitamim.file.picker.core.utils.getMimeType

class GetMediaLocation {

    suspend operator fun invoke(mediaElement: MediaElement): MediaLocation? {
        val path = mediaElement.resource.getPath() ?: return null
        val mimeType = mediaElement.mimeType
            ?: getMimeType(path)
            ?: mediaElement.defaultMimeType

        return MediaLocation(path, mimeType)
    }
}
