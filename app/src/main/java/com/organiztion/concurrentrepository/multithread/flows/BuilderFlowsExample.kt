package com.organiztion.concurrentrepository.multithread.flows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

/**
 * Пример 1.1
 *
 * flow - создает холодный поток, поток реализовывается приватным классом SafeFlow
 */
private fun Example1_1() {
    flow {
        emit(10)
    }
}

/**
 * Пример 1.2
 *
 * asFlow - переводит список в последовательность, работая с flow (и SafeFlow соответственно)
 */
private fun Example1_2() {
    listOf(10, 20).asFlow()
}

/**
 * Пример 1.3
 *
 * flowOf - берет набор элементов и переводит их в flow (и SafeFlow соответственно)
 */
private fun Example1_3() {
    flowOf(10, 20, 30)
}

/**
 * Пример 1.4
 *
 * emptyFlow - возвращает синглтон объект EmptyFlow, при вызове collect никогда не закончится
 */
private fun Example1_4() {
    val flow = emptyFlow<Int>()
}

/**
 * Пример 1.5
 *
 * channelFlow - возвращает отдельный канал, который позволяет обрабатывать значения не одному подписчкику (как в целом у каналов), а нескольким.
 * При этом изначально flow является холодным потоком (то есть запустится только при наличии подписчика)
 */
private fun Example1_5() {
    val scope = CoroutineScope(Job())
    val myFlow = channelFlow {
        println("Start")
        delay(2000)
        send(10)
        delay(2000)
        send(15)
        println("End")
    }

    scope.launch {
        myFlow.collect {
            println("Check 1: $it")
        }
    }
    scope.launch {
        myFlow.collect {
            println("Check 2: $it")
        }
    }
}


/**
 * Пример 1.6
 *
 * callbackFlow - представляет собой обертку, которую мы можем использовать для подписки и отписки от Api, которые
 * у себя под капотом используют паттерн API-Callback
 */
private fun Example1_6() {
    class API() {
        fun registerCallback(callback: () -> Unit) {}
        fun unregisterCallback(callback: () -> Unit) {}
    }

    val api = API()
    callbackFlow<Int> {
        val myCallback = {

        }
        api.registerCallback(myCallback)

        invokeOnClose {
            api.unregisterCallback(myCallback)
        }
    }
}

fun main() {
    Example1_6()
    while (true);
}