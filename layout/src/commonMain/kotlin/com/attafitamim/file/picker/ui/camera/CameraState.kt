package com.attafitamim.file.picker.ui.camera

sealed interface CameraState {
    sealed interface Video : CameraState {
        data object Start : Video
        data object Stop : Video
    }

    sealed interface Photo : CameraState {
        data object Capture : Photo
    }
}
