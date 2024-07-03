package com.attafitamim.file.picker.presentation.contract.permission

interface IPermissionHelper {

    suspend fun isCameraPermissionGranted(): Boolean
    suspend fun requestCameraPermission(): Boolean

    suspend fun isReadStoragePermissionGranted(): Boolean
    suspend fun requestReadStoragePermission(): Boolean

    /**
     * Be careful with these methods. On Android with api 30+ make sure you're able to write
     * to special folder: images, videos, etc.
     */
    suspend fun isWriteStoragePermissionGranted(): Boolean
    suspend fun requestWriteStoragePermission(): Boolean

    suspend fun isMediaPermissionGranted(): Boolean
    suspend fun requestMediaPermission(): Boolean

    suspend fun isLocationPermissionGranted(): Boolean
    suspend fun requestLocationPermission(): Boolean

    suspend fun isCoarseLocationPermissionGranted(): Boolean
    suspend fun requestCoarseLocationPermission(): Boolean

    suspend fun isBluetoothPermissionGranted(): Boolean
    suspend fun requestBluetoothPermission(): Boolean

    suspend fun isNotificationPermissionGranted(): Boolean
    suspend fun requestNotificationPermission(): Boolean

    suspend fun isMicrophonePermissionGranted(): Boolean
    suspend fun requestMicrophonePermission(): Boolean

    suspend fun isContactsPermissionGranted(): Boolean
    suspend fun requestContactsPermission(): Boolean

    suspend fun isBodySensorsPermissionGranted(): Boolean
    suspend fun requestBodySensorsPermission(): Boolean

    suspend fun isGalleryPermissionGranted(): Boolean
    suspend fun requestGalleryPermission(): Boolean

    suspend fun isCameraCapturePermissionsGranted(): Boolean
    suspend fun requestCameraCapturePermissions(): Boolean
}
