package com.organiztion.concurrentrepository.multithread.flows

import androidx.core.graphics.createBitmap
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

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
 * emptyFlow - возвращает синглтон объект EmptyFlow, при вызове collect никогда не закончится
 */
private fun Example1_5() {
    channelFlow {
        send(10)
    }
}

fun main() {
    Example1_1()
    while (true);
}