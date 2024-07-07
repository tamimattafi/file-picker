package com.attafitamim.file.picker.camera

sealed interface CameraSelector {
    data object Front : CameraSelector
    data object Back : CameraSelector
}
