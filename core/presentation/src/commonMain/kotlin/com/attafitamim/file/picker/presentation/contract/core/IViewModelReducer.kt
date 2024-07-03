package com.attafitamim.file.picker.presentation.contract.core

interface IViewModelReducer<VM_STATE : Any, UI_STATE : Any> {
    fun reduce(viewModelState: VM_STATE): UI_STATE
}
