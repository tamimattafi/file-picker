package com.attafitamim.file.picker.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

suspend fun <T : Any> async(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    action: suspend CoroutineScope.() -> T,
): Deferred<T> = coroutineScope {
    async(context = coroutineContext + Job(), start = start) { action() }
}

fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> = flow {
    var windowStartTime = currentTimeInMillis()
    var emitted = false
    collect { value ->
        val currentTime = currentTimeInMillis()
        val delta = currentTime - windowStartTime
        if (delta >= windowDuration) {
            windowStartTime += delta / windowDuration * windowDuration
            emitted = false
        }
        if (!emitted) {
            emit(value)
            emitted = true
        }
    }
}
