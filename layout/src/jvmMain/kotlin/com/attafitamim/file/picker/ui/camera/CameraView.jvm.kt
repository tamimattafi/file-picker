package com.attafitamim.file.picker.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 *  Composable function used to display camera preview and take a picture
 *
 * 	@param modifier Modifier to be applied to the preview window
 * 	@param cameraSelector value specifying whether engage front or back camera
 * 	@param frameFrequency long value designating how often [onFrame] should be invoked
 * 	@param cameraState [Channel] consisting of instances of [CameraState.Photo].
 * 	On each emission process of forming an image from camera's input is triggered
 * 	@param onImageCaptured callback that returns [ByteArray] representing the image
 * 	@param onError callback that is invoked when an error occurs
 * 	@param onFrame callback that is invoked every [frameFrequency] milliseconds and returns [ByteArray] representing the image
 */
@Composable
actual fun CameraView(
    modifier: Modifier,
    cameraSelector: CameraSelector,
    torchMode: CameraTorchMode,
    frameFrequency: Long,
    cameraState: CameraState?,
    videoPath: String?,
    onImageCaptured: ((ByteArray) -> Unit)?,
    onError: (() -> Unit)?,
    onVideoCaptureStart: (() -> Unit)?,
    onVideoCaptureStop: (() -> Unit)?,
    onFrame: ((ByteArray) -> Unit)?
) {
}