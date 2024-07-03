package com.attafitamim.file.picker.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun convertMillisToLocalDate(
    timeInSeconds: Long,
    format: String,
    locale: Locale = Locale.US
): String {
    val currentDate = Date(timeInSeconds)
    val dateFormatter = SimpleDateFormat(format, locale)
    return dateFormatter.format(currentDate)
}