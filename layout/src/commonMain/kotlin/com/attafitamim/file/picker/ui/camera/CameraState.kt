package com.attafitamim.file.picker.ui.camera

import androidx.compose.runtime.Stable

@Stable
sealed interface CameraState {

    @Stable
    sealed interface Video : CameraState {

        @Stable
        data object Start : Video

        @Stable
        data object Stop : Video
    }

    @Stable
    sealed interface Photo : CameraState {

        @Stable
        data object Capture : Photo
    }
}
