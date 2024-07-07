package com.attafitamim.file.picker.core.utils

import kotlinx.datetime.Clock

const val SECOND_IN_MILLIS = 1000L

fun currentTimeInMillis() = Clock.System.now().toEpochMilliseconds()