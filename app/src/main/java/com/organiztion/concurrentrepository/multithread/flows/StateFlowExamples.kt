package com.organiztion.concurrentrepository.multithread.flows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


/**
 * Пример 1.1
 *
 * StateFlow - это подвид SharedFlow, у которого установлены параметры, где буфер равен replay=1.
 * При этом мы изначально указываем значение, которое будет использоваться при подписке на последовательность
 */
private fun Example1_1() {
    val scope = CoroutineScope(Job())
    val state =
        MutableStateFlow(0) // Здесь мы указали начальное значение 0, при подписке на последовательность мы его и получим

    scope.launch {
        state.collect { value ->
            println("Current value $value")
        }
    }
}

/**
 * Пример 1.2
 *
 * У StateFlow есть несколько конструкторов для работы
 */
private fun Example1_2() {
    val scope = CoroutineScope(Job())
    val firstFlow = MutableStateFlow(0) // Стандартный конструктор

    // Запуск stateIn происходит в указываемой нами области. Мы ожидаем получение первого значения и дальше можем использовать для отслеживания
    scope.launch {
        val secondFlow = flow {
            while (true) {
                delay(3000)
                emit((1..100).random())
            }
        }.stateIn(this)
        secondFlow.collect { value ->
            println("Second flow $value")
        }
    }

    // Конструторк stateIn, принимающий scope-обработчик, когда он должен запуститься и начальное значение
    val thirdFlow = flow {
        while (true) {
            delay(3000)
            emit(10)
        }
    }.stateIn(scope, SharingStarted.Eagerly, 10)
}

/**
 * Пример 2.1
 *
 * При работе с StateFlow важно понимать что этот класс создавался как отдельный поток для хранения
 * определенного состояния. Поэтому, если мы обновляем наше состояние через поле value текущим же значением,
 * то наше значение не поменяется и подписчики не получат изменения
 */
private fun Example2_1() {
    val scope = CoroutineScope(Job())
    val firstFlow = MutableStateFlow(false)

    scope.launch {
        launch {
            // Придет только одно значение
            firstFlow.collect { value ->
                println("Current value $value")
            }
        }

        delay(1000)

        firstFlow.emit(false) // Посылаем тоже самое значение

        delay(1000)

        println("Try send status: " + firstFlow.tryEmit(false)) // Может быть мы просто не можем послать?

        delay(1000)

        println(
            "Try compare and set status: " + firstFlow.compareAndSet(
                false,
                false
            )
        ) // Попробуем сравнить и послать

        delay(1000)

        firstFlow.value = false // Посылаем тоже самое значение, но другим способом
    }
}

/**
 * Пример 2.2
 *
 * При этом даже если использовать StateFlow вместе с SharedFlow как upstream, то мы не сможем получить
 * одинаковые значения
 */
private fun Example2_2() {
    val scope = CoroutineScope(Job())
    val firstFlow = MutableSharedFlow<Boolean>()
    val firstStateFlow = firstFlow.stateIn(scope, SharingStarted.Eagerly, false)

    scope.launch {
        launch {
            firstStateFlow.collect { value ->
                println("State flow $value")
            }
        }

        launch {
            firstFlow.collect { value ->
                println("Shared flow $value")
            }
        }

        delay(2000)

        firstFlow.emit(false) // Посылаем тоже самое значение

        delay(1000)

        firstFlow.emit(false)

        delay(1000)

        firstFlow.emit(false)

        delay(1000)

        firstFlow.emit(false)

        delay(1000)

        firstFlow.emit(false)

        delay(1000)

        firstFlow.emit(true) // Ловля на живчика
    }
}

fun main() {
    Example2_2()
    while (true);
}