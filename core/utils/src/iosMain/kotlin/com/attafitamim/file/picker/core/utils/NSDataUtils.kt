@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package com.attafitamim.file.picker.core.utils

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.getBytes

private val emptyArray = ByteArray(0)
private val emptyNSData = NSData()

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    if (length.convert<Int>() == 0) return emptyArray

    return ByteArray(length.convert()).apply {
        usePinned {
            getBytes(it.addressOf(0), length)
        }
    }
}

fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return emptyNSData

    return memScoped {
        NSData.create(
            bytes = allocArrayOf(this@toNSData),
            length = this@toNSData.size.toULong()
        )
    }
}