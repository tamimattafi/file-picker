package com.attafitamim.file.picker.presentation.contract.camera

import com.attafitamim.file.picker.core.domain.model.media.MediaElement
import com.attafitamim.file.picker.presentation.contract.core.IStateViewModel

interface ICameraDelegate : IStateViewModel<CameraState, CameraSideEffect> {
    suspend fun onImageSaved(mediaElement: MediaElement) {}
    suspend fun onCameraSetupError() {}
    suspend fun onFlashSwitchError() {}
    suspend fun onCameraSwitchError() {}
    suspend fun exit() {}
}
