package com.attafitamim.file.picker.presentation.vm.camera

import com.attafitamim.file.picker.core.domain.interaction.media.AddImageMedia
import com.attafitamim.file.picker.core.domain.model.media.RawMedia
import com.attafitamim.file.picker.presentation.contract.camera.CameraSideEffect
import com.attafitamim.file.picker.presentation.contract.camera.CameraState
import com.attafitamim.file.picker.presentation.contract.camera.ICameraDelegate
import com.attafitamim.file.picker.presentation.contract.camera.ICameraViewModel
import com.attafitamim.file.picker.presentation.contract.core.IViewModelReducer
import com.attafitamim.file.picker.presentation.contract.permission.IPermissionHelper
import com.attafitamim.file.picker.presentation.vm.base.BaseStateViewModel
import com.attafitamim.file.picker.utils.MIME_TYPE_IMAGE_JPEG
import org.orbitmvi.orbit.syntax.simple.intent

// TODO use access manager
class CameraViewModel(
    private val permissionHelper: IPermissionHelper,
    private val delegate: ICameraDelegate,
    private val addImageMedia: AddImageMedia,
    reducer: IViewModelReducer<CameraModelState, CameraState>
) : BaseStateViewModel<CameraState, CameraModelState, CameraSideEffect>(
    reducer
), ICameraViewModel {

    override val initialModelState = CameraModelState()

    init {
        loadData()
    }

    private fun loadData() = intent {
        val isCameraGranted = permissionHelper.isCameraPermissionGranted() &&
            permissionHelper.isReadStoragePermissionGranted()

        if (!isCameraGranted) {
            exit()
            onCameraSetupError()
        }

        updateViewModelState {
            copy(isPermissionGranted = isCameraGranted)
        }
    }

    override fun exit() = intent {
        delegate.exit()
    }

    override fun handleRawImage(bytes: ByteArray, width: Int, height: Int) = intent {
        val rawMedia = RawMedia(bytes, width, height)
        updateViewModelState {
            copy(rawImage = rawMedia)
        }
    }

    override fun saveImage(title: String) = intent {
        val imageBytes = getViewModelState().rawImage ?: return@intent
        val mediaElement = addImageMedia(imageBytes, title, MIME_TYPE_IMAGE_JPEG)
        delegate.onImageSaved(mediaElement)
    }

    override fun onCameraSetupError() = intent {
        delegate.onCameraSetupError()
    }

    override fun onFlashSwitchError() = intent {
        delegate.onFlashSwitchError()
    }

    override fun onCameraSwitchError() = intent {
        delegate.onCameraSwitchError()
    }
}
