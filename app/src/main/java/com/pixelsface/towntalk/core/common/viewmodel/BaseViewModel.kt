package com.pixelsface.towntalk.core.common.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.TownTalkException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel class that provides common functionality for all ViewModels.
 */
abstract class BaseViewModel<State, Event> : ViewModel() {
    
    private val _state = MutableStateFlow(createInitialState())
    val state: StateFlow<State> = _state.asStateFlow()
    
    private val _event = MutableSharedFlow<Event>()
    val event: SharedFlow<Event> = _event.asSharedFlow()
    
    /**
     * Creates the initial state for the ViewModel.
     */
    protected abstract fun createInitialState(): State
    
    /**
     * Updates the state with the provided block.
     */
    protected fun updateState(block: State.() -> State) {
        _state.value = _state.value.block()
    }
    
    /**
     * Emits an event.
     */
    protected fun emitEvent(event: Event) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
    
    /**
     * Executes a block and handles the result.
     * If the block returns a success, calls onSuccess.
     * If the block returns an error, calls onError.
     */
    protected fun <T> handleResult(
        block: suspend () -> Result<T>,
        onSuccess: (T) -> Unit = {},
        onError: (TownTalkException) -> Unit = { emitEvent(it as Event) }
    ) {
        viewModelScope.launch {
            when (val result = block()) {
                is Result.Success -> onSuccess(result.data)
                is Result.Error -> onError(result.exception)
            }
        }
    }
    
    /**
     * Executes a block and handles the result.
     * If the block returns a success, calls onSuccess and updates the state.
     * If the block returns an error, calls onError.
     */
    protected fun <T> handleResultWithState(
        block: suspend () -> Result<T>,
        onSuccess: State.(T) -> State,
        onError: (TownTalkException) -> Unit = { emitEvent(it as Event) }
    ) {
        viewModelScope.launch {
            when (val result = block()) {
                is Result.Success -> updateState { onSuccess(result.data) }
                is Result.Error -> onError(result.exception)
            }
        }
    }
} 