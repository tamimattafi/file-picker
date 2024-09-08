package com.attafitamim.file.picker.core.domain.interaction.media

import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaLocation
import com.attafitamim.file.picker.core.domain.utils.defaultMimeType
import com.attafitamim.file.picker.core.domain.utils.getPath
import com.attafitamim.file.picker.core.utils.getMimeType

class GetMediaLocations {

    suspend operator fun invoke(mediaElements: List<MediaElement>) =
        mediaElements.mapNotNull { mediaElement ->
            val path = mediaElement.resource.getPath() ?: return@mapNotNull null
            val mimeType = mediaElement.mimeType
                ?: getMimeType(path)
                ?: mediaElement.defaultMimeType

            MediaLocation(path, mimeType)
        }
}
