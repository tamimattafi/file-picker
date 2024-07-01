package com.attafitamim.file.picker.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun <T : Any> async(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    action: suspend CoroutineScope.() -> T,
): Deferred<T> = coroutineScope {
    async(context = coroutineContext + Job(), start = start) { action() }
}


