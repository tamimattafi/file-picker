package com.attafitamim.file.picker.presentation.contract.camera

interface ICameraViewModel {
    fun exit()

    fun handleRawImage(bytes: ByteArray, width: Int, height: Int)
    fun saveImage(title: String)

    fun onCameraSetupError()
    fun onFlashSwitchError()
    fun onCameraSwitchError()
}
