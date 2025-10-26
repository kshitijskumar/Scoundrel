package org.example.scoundrel.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<STATE, INTENT, EFFECT> {

    protected val viewModelScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    protected abstract fun initialState(): STATE

    private val _state = MutableStateFlow<STATE>(initialState())
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<EFFECT>()
    val effect: Flow<EFFECT> = _effect.asSharedFlow()

    abstract fun processIntent(intent: INTENT)

    protected fun updateState(block: (STATE) -> STATE) {
        _state.update(block)
    }

    protected fun emitEffect(effect: EFFECT) {
        _effect.tryEmit(effect)
    }

}