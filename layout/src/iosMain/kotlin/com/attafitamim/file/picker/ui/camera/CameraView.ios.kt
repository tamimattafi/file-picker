package com.attafitamim.file.picker.ui.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.attafitamim.file.picker.core.utils.throttleFirst
import com.attafitamim.file.picker.core.utils.toByteArray
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.readValue
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import platform.AVFoundation.AVAuthorizationStatus
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDualWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInDuoCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInUltraWideCamera
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureFlashModeAuto
import platform.AVFoundation.AVCaptureFlashModeOff
import platform.AVFoundation.AVCaptureFlashModeOn
import platform.AVFoundation.AVCaptureInput
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCapturePhoto
import platform.AVFoundation.AVCapturePhotoCaptureDelegateProtocol
import platform.AVFoundation.AVCapturePhotoOutput
import platform.AVFoundation.AVCapturePhotoSettings
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureTorchModeAuto
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoDataOutput
import platform.AVFoundation.AVCaptureVideoDataOutputSampleBufferDelegateProtocol
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVVideoCodecKey
import platform.AVFoundation.AVVideoCodecTypeJPEG
import platform.AVFoundation.CGImageRepresentation
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.AVFoundation.deviceType
import platform.AVFoundation.fileDataRepresentation
import platform.AVFoundation.isFlashModeSupported
import platform.AVFoundation.isTorchModeSupported
import platform.AVFoundation.position
import platform.AVFoundation.setFlashMode
import platform.AVFoundation.setTorchMode
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectZero
import platform.CoreImage.CIImage
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferRef
import platform.CoreVideo.kCVPixelBufferPixelFormatTypeKey
import platform.CoreVideo.kCVPixelFormatType_32RGBA
import platform.Foundation.NSError
import platform.Foundation.NSFileManager
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIColor
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIDeviceOrientationDidChangeNotification
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImageOrientation.UIImageOrientationRight
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerCameraDevice
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_get_global_queue
import platform.posix.QOS_CLASS_BACKGROUND
import platform.posix.memcpy


private const val COMPRESSION_QUALITY = 1.0
private const val SEMICOLON = ":"
private const val BUFFER_CAPACITY = 10


private val deviceTypes = listOf(
    AVCaptureDeviceTypeBuiltInWideAngleCamera,
    AVCaptureDeviceTypeBuiltInDualWideCamera,
    AVCaptureDeviceTypeBuiltInDualCamera,
    AVCaptureDeviceTypeBuiltInUltraWideCamera,
    AVCaptureDeviceTypeBuiltInDuoCamera
)

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
    val status: AVAuthorizationStatus = remember {
        AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
    }

    val isCameraAvailable = remember {
        UIImagePickerController.isCameraDeviceAvailable(
            UIImagePickerControllerCameraDevice.UIImagePickerControllerCameraDeviceRear
        )
    }
    if (status == AVAuthorizationStatusAuthorized && isCameraAvailable) {
        LocalCameraView(
            modifier = modifier,
            cameraSelector = cameraSelector,
            torchMode = torchMode,
            frameFrequency = frameFrequency,
            cameraState = cameraState,
            videoPath = videoPath,
            onImageCaptured = onImageCaptured,
            onError = onError,
            onVideoCaptureStart = onVideoCaptureStart,
            onVideoCaptureStop = onVideoCaptureStop,
            onFrame = onFrame
        )
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
private fun LocalCameraView(
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
    val flow = remember {
        MutableSharedFlow<ByteArray>(extraBufferCapacity = BUFFER_CAPACITY)
    }

    val iosCameraSelector = remember(cameraSelector) {
        when (cameraSelector) {
            CameraSelector.FRONT -> AVCaptureDevicePositionBack
            CameraSelector.BACK -> AVCaptureDevicePositionFront
        }
    }

    val camera: AVCaptureDevice? = remember(iosCameraSelector) {
        AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            deviceTypes = deviceTypes,
            mediaType = AVMediaTypeVideo,
            position = iosCameraSelector
        ).devices.firstOrNull() as? AVCaptureDevice
    }

    val capturePhotoOutput = remember { AVCapturePhotoOutput() }
    val captureFrameOutput = remember {
        AVCaptureVideoDataOutput().apply {
            this.setVideoSettings(
                mapOf(
                    kCVPixelBufferPixelFormatTypeKey to kCVPixelFormatType_32RGBA,
                    AVVideoCodecKey to AVVideoCodecTypeJPEG
                )
            )
        }
    }

    var actualOrientation by remember { mutableStateOf(AVCaptureVideoOrientationPortrait) }
    var captureVideoStarted by remember { mutableStateOf(false) }
    val captureVideoFileOutput = remember(videoPath) { AVCaptureMovieFileOutput() }
    val captureSession: AVCaptureSession = remember {
        AVCaptureSession().also { captureSession ->
            captureSession.sessionPreset = AVCaptureSessionPresetHigh
            if (camera != null) {
                val captureDeviceInput = AVCaptureDeviceInput(device = camera, error = null)
                val captureDeviceAudioInput: AVCaptureDeviceInput? =
                    AVCaptureDeviceInput.deviceInputWithDevice(
                        device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeAudio)!!,
                        error = null
                    )
                captureSession.addInput(captureDeviceInput)
                captureDeviceAudioInput?.let {
                    captureSession.addInput(captureDeviceAudioInput)
                }
            }
            captureSession.addOutput(capturePhotoOutput)
        }
    }
    val cameraPreviewLayer = remember {
        AVCaptureVideoPreviewLayer(session = captureSession)
    }

    val recordingDelegate = object : NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {
        override fun captureOutput(
            output: AVCaptureFileOutput,
            didFinishRecordingToOutputFileAtURL: NSURL,
            fromConnections: List<*>,
            error: NSError?
        ) {
            if (error != null) {
                onError?.invoke()
            }
            onVideoCaptureStop?.invoke()
        }
    }
    val frameQueue = remember {
        dispatch_get_global_queue(QOS_CLASS_BACKGROUND.convert(), 0.convert())
    }

    val photoCaptureDelegate = remember {
        object : NSObject(), AVCapturePhotoCaptureDelegateProtocol {
            override fun captureOutput(
                output: AVCapturePhotoOutput,
                didFinishProcessingPhoto: AVCapturePhoto,
                error: NSError?
            ) {
                val photoData = didFinishProcessingPhoto.CGImageRepresentation()
                if (photoData != null) {
                    val image = UIImage(photoData, COMPRESSION_QUALITY, UIImageOrientationRight)
                    UIImageJPEGRepresentation(image, COMPRESSION_QUALITY)?.let { jpg ->
                        val byteArray = ByteArray(jpg.length.toInt()).apply {
                            usePinned { pinned ->
                                memcpy(pinned.addressOf(0), jpg.bytes, jpg.length)
                            }
                        }
                        onImageCaptured?.invoke(byteArray)
                    }
                } else {
                    onError?.invoke()
                }
            }
        }
    }
    val frameCaptureDelegate = remember {
        object : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
            override fun captureOutput(
                output: AVCaptureOutput,
                didOutputSampleBuffer: CMSampleBufferRef?,
                fromConnection: AVCaptureConnection
            ) {
                convertSampleBufferToJPEGData(didOutputSampleBuffer)?.let { frame ->
                    flow.tryEmit(frame)
                }
            }
        }
    }

    fun capturePhoto(photoCaptureDelegateProtocol: AVCapturePhotoCaptureDelegateProtocol) {
        val photoSettings = AVCapturePhotoSettings.photoSettingsWithFormat(
            format = mapOf(AVVideoCodecKey to AVVideoCodecTypeJPEG)
        )
        if (camera?.position == AVCaptureDevicePositionFront) {
            capturePhotoOutput.connectionWithMediaType(AVMediaTypeVideo)
                ?.automaticallyAdjustsVideoMirroring = false
            capturePhotoOutput.connectionWithMediaType(AVMediaTypeVideo)
                ?.videoMirrored = true
        }
        capturePhotoOutput.capturePhotoWithSettings(
            settings = photoSettings,
            delegate = photoCaptureDelegateProtocol
        )
    }

    LaunchedEffect(cameraSelector) {
        captureSession.beginConfiguration()

        captureSession.inputs.firstOrNull { input ->
            (input as? AVCaptureDeviceInput)?.device?.deviceType in deviceTypes
        }?.let { input ->
            captureSession.removeInput(input as AVCaptureInput)
        }

        val position = when (cameraSelector) {
            CameraSelector.BACK -> AVCaptureDevicePositionBack
            CameraSelector.FRONT -> AVCaptureDevicePositionFront
        }
        val captureDeviceInput = AVCaptureDeviceInput(
            deviceTypes.first { type ->
                AVCaptureDevice.defaultDeviceWithDeviceType(
                    type,
                    AVMediaTypeVideo,
                    position
                ) != null
            }.let { type ->
                AVCaptureDevice.defaultDeviceWithDeviceType(type, AVMediaTypeVideo, position)
            }!!,
            null
        )

        captureDeviceInput.let(captureSession::addInput)
        captureSession.commitConfiguration()
    }

    LaunchedEffect(cameraState, videoPath) {
        when (cameraState) {
            CameraState.Photo.Capture -> {
                capturePhoto(photoCaptureDelegate)
            }

            CameraState.Video.Start -> {
                captureVideoStarted = true
                if (camera?.position == AVCaptureDevicePositionFront) {
                    capturePhotoOutput.connectionWithMediaType(AVMediaTypeVideo)
                        ?.automaticallyAdjustsVideoMirroring = false
                    capturePhotoOutput.connectionWithMediaType(AVMediaTypeVideo)
                        ?.videoMirrored = true
                }
                captureSession.addOutput(captureVideoFileOutput)

                if (videoPath != null) {
                    val filePath = NSURL.fileURLWithPath(videoPath)
                    runCatching {
                        NSFileManager.defaultManager().removeItemAtURL(filePath, null)
                    }
                    captureVideoFileOutput.startRecordingToOutputFileURL(
                        filePath,
                        recordingDelegate
                    )
                    onVideoCaptureStart?.invoke()
                }
            }

            CameraState.Video.Stop -> {
                captureVideoStarted = false
                captureVideoFileOutput.stopRecording()
            }

            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        if (onFrame != null) {
            captureFrameOutput.setSampleBufferDelegate(frameCaptureDelegate, frameQueue)
            captureSession.addOutput(captureFrameOutput)

            flow.throttleFirst(frameFrequency).collect { bytes ->
                runCatching {
                    onFrame.invoke(bytes)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        class OrientationListener : NSObject() {
            @Suppress("UNUSED_PARAMETER")
            @ObjCAction
            fun orientationDidChange(arg: NSNotification) {
                val cameraConnection = cameraPreviewLayer.connection
                if (cameraConnection != null) {
                    actualOrientation = when (UIDevice.currentDevice.orientation) {
                        UIDeviceOrientation.UIDeviceOrientationPortrait ->
                            AVCaptureVideoOrientationPortrait

                        UIDeviceOrientation.UIDeviceOrientationLandscapeLeft ->
                            AVCaptureVideoOrientationLandscapeRight

                        UIDeviceOrientation.UIDeviceOrientationLandscapeRight ->
                            AVCaptureVideoOrientationLandscapeLeft

                        UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown ->
                            AVCaptureVideoOrientationPortrait

                        else -> cameraConnection.videoOrientation
                    }
                    cameraConnection.videoOrientation = actualOrientation
                }
                capturePhotoOutput.connectionWithMediaType(AVMediaTypeVideo)
                    ?.videoOrientation = actualOrientation
                captureFrameOutput.connectionWithMediaType(AVMediaTypeVideo)
                    ?.videoOrientation = actualOrientation
            }
        }

        val listener = OrientationListener()
        val notificationName = UIDeviceOrientationDidChangeNotification
        NSNotificationCenter.defaultCenter.addObserver(
            observer = listener,
            selector = NSSelectorFromString(
                OrientationListener::orientationDidChange.name + SEMICOLON
            ),
            name = notificationName,
            `object` = null
        )
        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(
                observer = listener,
                name = notificationName,
                `object` = null
            )
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            captureSession.startRunning()
        }
    }

    LaunchedEffect(torchMode, captureSession.inputs) {
        captureSession.inputs.forEach { input ->
            val device = (input as? AVCaptureDeviceInput)?.device?.takeIf { device ->
                device.deviceType in deviceTypes
            } ?: return@forEach

            device.lockForConfiguration(null)
            val iosTorchMode = when (torchMode) {
                CameraTorchMode.ON -> AVCaptureTorchModeOn
                CameraTorchMode.OFF -> AVCaptureTorchModeOff
                CameraTorchMode.AUTO -> AVCaptureTorchModeAuto
            }

            if (device.isTorchModeSupported(iosTorchMode)) {
                device.setTorchMode(iosTorchMode)
            }

            val iosFlashMode = when (torchMode) {
                CameraTorchMode.ON -> AVCaptureFlashModeOn
                CameraTorchMode.OFF -> AVCaptureFlashModeOff
                CameraTorchMode.AUTO -> AVCaptureFlashModeAuto
            }

            if (device.isFlashModeSupported(iosFlashMode)) {
                device.setFlashMode(iosFlashMode)
            }

            device.unlockForConfiguration()
        }
    }

    UIKitView(
        factory = {
            cameraPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            val cameraContainer = object : UIView(frame = CGRectZero.readValue()) {
                override fun layoutSubviews() {
                    super.layoutSubviews()
                    cameraPreviewLayer.frame = this.bounds
                }
            }.apply { setBackgroundColor(UIColor.blackColor) }
            captureFrameOutput.connectionWithMediaType(AVMediaTypeVideo)
                ?.videoOrientation = actualOrientation
            cameraContainer.layer.addSublayer(cameraPreviewLayer)
            cameraPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            cameraContainer
        },
        modifier = modifier,
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        )
    )
}

@OptIn(ExperimentalForeignApi::class)
fun convertSampleBufferToJPEGData(
    sampleBuffer: CMSampleBufferRef?
): ByteArray? {
    val imageBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) ?: return null
    val ciImage = CIImage.imageWithCVPixelBuffer(imageBuffer)
    val uiImage = UIImage.imageWithCIImage(ciImage)
    return UIImagePNGRepresentation(uiImage)?.toByteArray() ?: return null
}