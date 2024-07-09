package com.attafitamim.file.picker.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.channels.Channel

internal const val DEFAULT_FRAME_FREQUENCY = 300L

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
expect fun CameraView(
    modifier: Modifier = Modifier,
    cameraSelector: CameraSelector = CameraSelector.BACK,
    torchMode: CameraTorchMode = CameraTorchMode.OFF,
    frameFrequency: Long = DEFAULT_FRAME_FREQUENCY,
    cameraState: CameraState? = null,
    videoPath: String? = null,
    onImageCaptured: ((ByteArray) -> Unit)? = null,
    onError: (() -> Unit)? = null,
    onVideoCaptureStart: (() -> Unit)? = null,
    onVideoCaptureStop: (() -> Unit)? = null,
    onFrame: ((ByteArray) -> Unit)? = null
)
