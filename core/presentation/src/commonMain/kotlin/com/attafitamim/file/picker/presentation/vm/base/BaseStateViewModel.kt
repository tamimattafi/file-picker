package com.attafitamim.file.picker.presentation.vm.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attafitamim.file.picker.presentation.contract.core.IStateViewModel
import com.attafitamim.file.picker.presentation.contract.core.IViewModelReducer
import kotlin.concurrent.Volatile
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.container
import org.orbitmvi.orbit.syntax.simple.SimpleSyntax
import org.orbitmvi.orbit.syntax.simple.intent
import org.orbitmvi.orbit.syntax.simple.postSideEffect
import org.orbitmvi.orbit.syntax.simple.reduce

abstract class BaseStateViewModel<UI_STATE : Any, VM_STATE : Any, SIDE_EFFECT : Any>(
    private val reducer: IViewModelReducer<VM_STATE, UI_STATE>
) : ViewModel(), ContainerHost<UI_STATE, SIDE_EFFECT>, IStateViewModel<UI_STATE, SIDE_EFFECT> {

    override val container: Container<UI_STATE, SIDE_EFFECT> by lazy(LazyThreadSafetyMode.NONE) {
        viewModelScope.container(
            reducer.reduce(initialModelState),
            Container.Settings(exceptionHandler = coroutineExceptionHandler)
        ) {
            modelState = initialModelState
        }
    }

    override val stateFlow: StateFlow<UI_STATE>
        get() = container.stateFlow

    override val sideEffectFlow: Flow<SIDE_EFFECT>
        get() = container.sideEffectFlow

    protected abstract val initialModelState: VM_STATE

    @Volatile
    private lateinit var modelState: VM_STATE
    private val modelStateMutex = Mutex()

    private val singleJobHashMap = HashMap<String, Job>()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleThrowable(throwable)
    }

    // in compose side effects right before exiting screen may not be handled
    protected suspend fun SimpleSyntax<UI_STATE, SIDE_EFFECT>.postSideEffectWithDelay(
        effect: SIDE_EFFECT
    ) {
        postSideEffect(effect)
        delay(EFFECT_DELAY)
    }

    protected open fun handleThrowable(throwable: Throwable) {}

    protected suspend fun tryCancelSingleJob(
        jobKey: String,
        onSucceededCanceling: (suspend () -> Unit)? = null
    ) {
        val job = singleJobHashMap.remove(jobKey) ?: return

        job.cancel()
        onSucceededCanceling?.invoke()
    }

    protected fun cancelSingleJobs() {
        singleJobHashMap.values.forEach(Job::cancel)
        singleJobHashMap.clear()
    }

    protected fun singleIntent(
        key: String,
        transformer: suspend SimpleSyntax<UI_STATE, SIDE_EFFECT>.() -> Unit
    ) = intent {
        launchSingleJob(key) {
            transformer()
        }
    }

    protected fun mainIntent(
        transformer: suspend SimpleSyntax<UI_STATE, SIDE_EFFECT>.() -> Unit
    ) = intent {
        withContext(Dispatchers.Main.immediate) {
            transformer()
        }
    }

    protected suspend fun launchSingleJob(
        jobKey: String,
        errorHandler: CoroutineExceptionHandler? = null,
        action: suspend CoroutineScope.() -> Unit
    ) {
        tryCancelSingleJob(jobKey)

        launchJob(errorHandler, action)
            .let { job -> singleJobHashMap[jobKey] = job }
    }

    protected suspend fun launchJob(
        errorHandler: CoroutineExceptionHandler? = null,
        action: suspend CoroutineScope.() -> Unit
    ): Job {
        val runningContext = if (errorHandler != null) {
            container.settings.intentLaunchingDispatcher + errorHandler
        } else {
            container.settings.intentLaunchingDispatcher + coroutineContext
        }

        return viewModelScope.launch(
            context = runningContext,
            block = action
        )
    }

    protected suspend fun getViewModelState() = modelStateMutex.withLock {
        modelState
    }

    protected suspend fun SimpleSyntax<UI_STATE, *>.updateViewModelState(
        action: VM_STATE.() -> VM_STATE
    ) = modelStateMutex.withLock {
        val newModelState = modelState.action()
        if (newModelState == modelState) return@withLock

        modelState = newModelState
        reduce {
            reducer.reduce(modelState)
        }
    }

    override fun onCleared() {
        super.onCleared()
        singleJobHashMap.clear()
    }

    private companion object {

        const val EFFECT_DELAY = 100L
    }
}
