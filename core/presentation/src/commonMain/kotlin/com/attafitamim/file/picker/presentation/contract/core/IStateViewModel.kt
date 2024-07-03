package com.attafitamim.file.picker.presentation.contract.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IStateViewModel<STATE, SIDE_EFFECT> : IBaseViewModel {
    val stateFlow: StateFlow<STATE>
    val sideEffectFlow: Flow<SIDE_EFFECT>
}