package com.organiztion.concurrentrepository.multithread.flows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.math.exp


data class KeyExample1(val number: Int)

fun Example1() {
    val scope = CoroutineScope(Job())
    scope.produce {
        send(Any())
    }

    val _events = MutableSharedFlow<KeyExample1>()
    val events = _events.shareIn(scope, SharingStarted.Lazily, 1)
    scope.launch {
        _events.emit(KeyExample1(1))
    }

    scope.launch {
        events.collect {
            println("Example $it")
        }
    }
}

fun Example2() {
    val state = MutableStateFlow(Any())
    state.buffer()
    state.value = Any()
}

fun main() {
    Example1()
    while(true);
}