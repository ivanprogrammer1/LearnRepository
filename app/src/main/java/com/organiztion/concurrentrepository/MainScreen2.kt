package com.organiztion.concurrentrepository

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

val scope = CoroutineScope(Job())

fun main() {
    scope.launch {
        launch(CoroutineExceptionHandler { _, _ ->
            println("Catch error")
        }) {
            throw Exception("My exception")
        }.join()
    }

    while(scope.isActive);
}