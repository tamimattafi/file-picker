package com.attafitamim.file.picker.core.data.source.media

interface ILocalSourceMediaRetriever {

    fun handleInput(
        phAsset: MediaAsset,
        handler: IMediaElementHandler
    )
}