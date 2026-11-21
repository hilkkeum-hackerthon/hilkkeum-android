package kr.co.cmc.hil.presentation.common

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class MviViewModel<S : Any, A : Any, E : Any>(initialState: S) : ViewModel() {
    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<E>(replay = 0, extraBufferCapacity = 1)
    val event: SharedFlow<E> = _event.asSharedFlow()

    abstract fun onAction(action: A)

    protected fun updateState(transform: (S) -> S) {
        _uiState.update(transform)
    }

    protected suspend fun emitEvent(event: E) {
        _event.emit(event)
    }
}