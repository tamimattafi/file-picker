package com.attafitamim.file.picker.core.data.source.media

sealed interface IosMediaElement {

    data class Image(
        val path: String?,
        val date: Double?
    ) : IosMediaElement

    data class Video(
        val path: String?,
        val date: Double?,
        val duration: Double
    ) : IosMediaElement
}

fun interface IMediaElementHandler {

    fun handleElement(element: IosMediaElement?)
}

interface ILocalSourceMediaRetriever {

    fun handleInput(
        phAsset: MediaAsset,
        handler: IMediaElementHandler
    )
}
