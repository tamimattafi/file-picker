package com.attafitamim.file.picker.camera

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.internal.utils.ImageUtil
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val IMAGE_QUALITY = 100
private const val DEFAULT_ROTATION = 90

@SuppressLint("RestrictedApi")
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
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val frameExecutor = remember { Executors.newSingleThreadExecutor() }
    val frameScope = rememberCoroutineScope { Dispatchers.Default }

    val imageCapture = remember {
        ImageCapture.Builder().build()
    }

    val videoCapture = remember {
        VideoCapture.withOutput(
            Recorder.Builder().setQualitySelector(
                QualitySelector.from(
                    Quality.UHD,
                    FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                )
            ).build()
        )
    }

    LaunchedEffect(torchMode, imageCapture.camera, videoCapture.camera) {
        val isEnabled = torchMode != CameraTorchMode.OFF
        imageCapture.camera?.cameraControl?.enableTorch(isEnabled)
        videoCapture.camera?.cameraControl?.enableTorch(isEnabled)
    }

    val androidCameraSelector = remember(cameraSelector) {
        when (cameraSelector) {
            CameraSelector.BACK -> androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            CameraSelector.FRONT -> androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }
    val preview = remember { Preview.Builder().build() }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var isRecordingInProcess by remember { mutableStateOf(false) }

    fun startVideo() {
        if (videoPath == null) {
            onError?.invoke()
            return
        }

        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
            return
        }

        val fileOutputOptions = FileOutputOptions.Builder(File(videoPath)).build()
        recording = videoCapture.output.prepareRecording(context, fileOutputOptions)
            .apply {
                @SuppressLint("MissingPermission")
                if (PermissionChecker.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        onVideoCaptureStart?.invoke()
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (recordEvent.hasError()) {
                            onError?.invoke()
                        } else {
                            onVideoCaptureStop?.invoke()
                            recording?.close()
                            recording = null
                        }
                    }
                }
            }
    }

    LaunchedEffect(cameraState, videoPath) {
        cameraState?.let { state ->
            when (state) {
                CameraState.Photo.Capture -> {
                    imageCapture.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                onImageCaptured?.invoke(image.getByteArray(IMAGE_QUALITY))
                                image.close()
                            }

                            override fun onError(exception: ImageCaptureException) {
                                onError?.invoke()
                            }
                        }
                    )
                }
                CameraState.Video.Start -> {
                    startVideo()
                    isRecordingInProcess = true
                }

                CameraState.Video.Stop -> {
                    recording?.stop()
                    isRecordingInProcess = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (onFrame != null) {
            frameScope.launch {
                while (true) {
                    imageCapture.takePicture(
                        frameExecutor,
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                frameScope.launch {
                                    onFrame(image.getByteArray(IMAGE_QUALITY))
                                }
                            }
                        }
                    )
                    delay(frameFrequency)
                }
            }
        }
    }

    LaunchedEffect(androidCameraSelector) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            runCatching {
                if (isRecordingInProcess) {
                    cameraProvider.unbind(preview)
                    cameraProvider.unbind(imageCapture)
                } else {
                    cameraProvider.unbindAll()
                }
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    androidCameraSelector,
                    preview,
                    imageCapture,
                    videoCapture
                )
            }
        },
            ContextCompat.getMainExecutor(context)
        )
    }

    AndroidView(
        modifier = modifier.background(Color.Black),
        factory = { viewContext ->
            PreviewView(viewContext).also { previewView ->
                preview.setSurfaceProvider(previewView.surfaceProvider)
            }
        }
    )
}

@SuppressLint("RestrictedApi", "UnsafeOptInUsageError")
fun ImageProxy.getByteArray(quality: Int): ByteArray = use {
    val byteArray = when (image?.format) {
        ImageFormat.JPEG -> {
            val source = toBitmap()
            val matrix = Matrix()
            matrix.postRotate(DEFAULT_ROTATION.toFloat())
            val bitmap = Bitmap.createBitmap(
                source,
                0,
                0,
                source.width,
                source.height,
                matrix,
                true
            )

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        }

        ImageFormat.YUV_420_888 -> ImageUtil.yuvImageToJpegByteArray(
            this,
            this.cropRect,
            quality,
            DEFAULT_ROTATION
        )

        else -> {
            val buffer = planes.first().buffer
            buffer.rewind()

            ByteArray(buffer.capacity()).apply { buffer.get(this) }
        }
    }

    return byteArray
}

