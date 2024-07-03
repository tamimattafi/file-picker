package com.attafitamim.file.picker.presentation.vm.camera

import com.attafitamim.file.picker.core.domain.model.media.RawMedia

data class CameraModelState(
    val isPermissionGranted: Boolean = false,
    val rawImage: RawMedia? = null
)
