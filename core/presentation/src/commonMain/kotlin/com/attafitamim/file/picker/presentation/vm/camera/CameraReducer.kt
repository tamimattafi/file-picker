package com.attafitamim.file.picker.presentation.vm.camera

import com.attafitamim.file.picker.presentation.contract.camera.CameraState
import com.attafitamim.file.picker.presentation.contract.core.IViewModelReducer

class CameraReducer : IViewModelReducer<CameraModelState, CameraState> {

    override fun reduce(viewModelState: CameraModelState): CameraState = with(viewModelState) {
        return CameraState(
            isPermissionGranted
        )
    }
}
