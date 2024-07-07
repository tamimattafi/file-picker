package com.attafitamim.file.picker.camera

sealed interface CameraState {
    sealed interface Video : CameraState {
        data object Start : Video
        data object Stop : Video
    }

    sealed interface Photo : CameraState {
        data object Capture : Photo
    }
}
