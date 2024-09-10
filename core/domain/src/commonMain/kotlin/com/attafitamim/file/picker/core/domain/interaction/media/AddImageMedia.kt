package com.attafitamim.file.picker.core.domain.interaction.media

import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.core.domain.model.media.MediaLocation
import com.attafitamim.file.picker.core.domain.model.media.MediaRawData
import com.attafitamim.file.picker.core.domain.repository.media.IMediaRepository
import com.attafitamim.file.picker.core.utils.NAME_SEPARATOR
import com.attafitamim.file.picker.core.utils.currentTimeInMillis
import kotlin.random.Random

class AddImageMedia(
    private val mediaRepository: IMediaRepository
) {

    suspend operator fun invoke(
        mediaRawData: MediaRawData,
        mimeType: String,
        title: String? = null,
        time: Long? = null,
        description: String? = null,
        isDateEnabled: Boolean = true
    ): MediaLocation {
        val actualTime = time ?: currentTimeInMillis()
        val actualTitle = title ?: generateTitle(actualTime)
        return mediaRepository.addImage(
            mediaRawData.bytes,
            actualTitle,
            actualTime,
            mimeType,
            description,
            isDateEnabled
        )
    }

    private fun generateTitle(time: Long): String = buildString {
        append(
            time.toString(),
            NAME_SEPARATOR,
            Random.nextLong().toString()
        )
    }
}
