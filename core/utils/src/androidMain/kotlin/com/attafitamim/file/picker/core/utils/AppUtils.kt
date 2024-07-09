package com.attafitamim.file.picker.core.utils

import android.app.PendingIntent
import android.os.Build

val isSdk26AndHigher: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
val isSdk29AndHigher: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
val isSdk30AndHigher: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
val isSdk31AndHigher: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
val isSdk33AndHigher: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

fun getServiceIntentFlag(vararg subFlags: Int): Int {
    var resultFlag: Int = PendingIntent.FLAG_IMMUTABLE

    subFlags.forEach { subFlag ->
        resultFlag = resultFlag or subFlag
    }

    return resultFlag
}
