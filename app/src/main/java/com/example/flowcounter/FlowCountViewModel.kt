package com.example.flowcounter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.runningReduce
import kotlinx.coroutines.launch

private const val millisDelay = 100L

class FlowCountViewModel : ViewModel() {

    private val _resultFlow = MutableStateFlow("")
    val resultFlow: StateFlow<String> = _resultFlow

    private val _errorFlow = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val errorFlow: SharedFlow<Unit> = _errorFlow


    fun onButtonClick(inputString: String) {
        val inputNumber = try {
            inputString.toInt()
        } catch (ex: NumberFormatException) {
            _errorFlow.tryEmit(Unit)
            return
        }

        _resultFlow.value = ""

        val summedFlow = getSummedFlow(inputNumber)
        viewModelScope.launch {
            summedFlow.collect { number ->
                _resultFlow.value += "$number\n"
            }
        }
    }

    private fun getSummedFlow(inputNumber: Int) = (1..inputNumber).map { index ->
        flow {
            delay(index * millisDelay)
            emit(index)
        }
    }
        .merge()
        .runningReduce { sum, value ->
            sum + value
        }
}