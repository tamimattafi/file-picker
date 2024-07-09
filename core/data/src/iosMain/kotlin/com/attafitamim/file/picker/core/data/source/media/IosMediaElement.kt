package com.attafitamim.file.picker.core.data.source.media

sealed interface IosMediaElement

data class IosMediaElementVideo(
    val path: String?,
    val date: Double?,
    val duration: Double
) : IosMediaElement

data class IosMediaElementImage(
    val path: String?,
    val date: Double?
) : IosMediaElement

fun interface IMediaElementHandler {

    fun handleElement(element: IosMediaElement?)
}

interface ILocalSourceMediaRetriever {

    fun handleInput(
        phAsset: MediaAsset,
        handler: IMediaElementHandler
    )
}
