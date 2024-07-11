package com.attafitamim.file.picker.core.data.source.media

import com.attafitamim.file.picker.core.data.source.media.element.PlatformMediaElement

fun interface IMediaElementHandler {

    fun handleElement(element: PlatformMediaElement?)
}