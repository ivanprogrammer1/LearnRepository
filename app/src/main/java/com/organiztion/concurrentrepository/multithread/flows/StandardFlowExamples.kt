package com.organiztion.concurrentrepository.multithread.flows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * Пример 1.1
 *
 * В Kotlin для работы с последовательностями есть несколько видов потоков.
 *
 * Первый вид - sequences. Он содержит все основные операции, которые используется и во Flow
 *
 * Существует несколько конструкторов для создания последовательностей
 */
private fun Example1_1() {
    // Задаем последовательность через sequenceOf
    val elements = sequenceOf(15, 20, 30, 40, 50)

    // Задаем последовательность через генератор generateSequence, указываем функцию обновления значения и базовое значение
    generateSequence(10) { lastValue ->
        lastValue + 5
    }

    // Переводим список в последовательность
    listOf(10, 20, 30, 40).asSequence()

    // Переводим в последовательность через обычный итератор
    iterator {
        yield(10)
        yield(20)
        yield(30)
    }.asSequence()
}

/**
 * Пример 1.2
 *
 * Важно понимать, что sequences основан на генераторах.
 * Сколько значений нам нужно, столько мы и можем вызввать (если последовательность содержит в себе бесконечный счетчик)
 */
private fun Example1_2() {
    // Задачем список элементов
    val elements = sequence {
        var start = 0
        while (true) yield(start++)
    }
    println("Возьмем 10 элементов: " + elements.take(10).toList())
    println(
        "Можем еще раз спокойно взять 10 элементов, причем они будут такими же: " + elements.take(
            10
        ).toList()
    )
    println("Можем вызвать 20 элементов: " + elements.take(20).toList())
    val saverSequence = elements.take(20)
    println(
        "Мы не сможем вызвать больше элементов, потому что сами ограничили последовательность 20-ю: " + saverSequence.take(
            30
        ).toList()
    )
}

/**
 * Пример 1.3
 *
 * Рассмотрим варианты промежуточных операций
 */
private fun Example1_3() {
    // Задачем список элементов
    val elements = sequence {
        var start = 0
        while (true) yield(start++)
    }

    println(
        "Операция drop, исключает заданные с начала элементы: " + elements.take(20).drop(10)
            .toList()
    )

    println("Операция filter, фильтрует элементы по условию: " + elements.take(20).filter { it > 9 }
        .toList()
    )

    println(
        "Операция filterNot, фильтрует элементы по условию (но тут наоборот они не должны совпадать с ним): " + elements.take(
            20
        ).filterNot { it < 10 }.toList()
    )

    println(
        "Операция filterNotNull, работает с элементами, оставляя только не пустые): " + elements.take(
            20
        ).map { if (it % 2 == 0) null else it }.filterNotNull().toList()
    )

    println(
        "Операция map, позволяет изменять downstream, приводить в любой удобный нам вид: " + elements.take(
            20
        ).map { it.toDouble() }.toList()
    )

    println(
        "Операция onEach, позволяет проводить операцию над всеми приходящими элементами (например, логировать): " + elements.take(
            5
        ).onEach { println("Логируем элемент $it") }.toList()
    )

    println(
        "Операция take, позволяет брать то кол-во элементов, которое нам нужно: " + elements.take(
            16
        ).toList()
    )

    println("---- Существуют особые операции ----")
    val sequence1 = sequence {
        var start = 0
        while (true) yield(start++)
    }

    val sequence2 = sequence {
        var start = 0
        while (true) yield(start++)
    }

    val commonSequence = sequence1.zip(sequence2)
    println("Операция zip позволяет объединить между собой две последовательности в одну $commonSequence")
    println(
        "При работе с такой последовательностью мы будем получать значения сразу из двух ${
            commonSequence.take(
                10
            ).toList()
        }"
    )

    println(
        "Операция flatMap позволяет объединять значения из обеих последовательностей в одну ${
            commonSequence.take(10).flatMap { pair ->
                sequenceOf(pair.first, pair.second)
            }.toList()
        }"
    )

    println(
        "FlatMap как нельзя лучше показывает разницу работу при перемещении операторв:\n ${
            commonSequence.take(10).flatMap { pair ->
                sequenceOf(pair.first, pair.second)
            }.toList()
        }\nи\n ${
            commonSequence.flatMap { pair ->
                sequenceOf(pair.first, pair.second)
            }.take(10).toList()
        }"
    )
}

/**
 * Пример 2.1
 *
 * Второй вид потоков - основанный на Flow API
 * Работать с ними можно посредством нескольких конструкторов
 */
@OptIn(FlowPreview::class)
private fun Example2_1() {
    // Для работы с Flow нам уже понадобятся корутины, так как Flow завязан на них
    val scope = CoroutineScope(Job())

    // Работаем с Flow через конструктор flow (как sequence)
    val firstFlow = flow {
        var number = 0
        while (true) emit(number++)
    }

    // Задаем конкретный список элементов через flowOf
    val secondFlow = flowOf(1, 4, 5, 6, 7)

    // Переводит текущую коллекцию в поток, здесь важно наличие итератора у переводимого объекта
    val thirdFlow = listOf(1, 2, 3).asFlow()

    // Так, аналогично третьему потоку мы создаем список элементов, но уже через сам итератор
    val fourthFlow = iterator {
        yield(10)
        yield(50)
        yield(30)
    }.asFlow()
}

/**
 * Пример 2.2
 *
 * Работа с Flow и Sequences отличается. Например, основная терминальная операция для перевода последовательности в горячий вид используется операция collect
 * Важно учесть, что если последовательность является бесконечной, то вызов collect никогда не остановится (только при отмене job)
 */
@OptIn(FlowPreview::class)
private fun Example2_2() {
    val scope = CoroutineScope(Job())
    val firstFlow = flow {
        var number = 0
        while (true) emit(number++)
    }

    scope.launch {
        try {
            withTimeout(50) {
                firstFlow.collect {
                    println("Будем работать бесконечно $it")
                }
            }
        } catch (exception: Exception) {

        }

        firstFlow.take(10).collect {
            println("Здесь уже ограничено только первыми 10 значениями: $it")
        }
    }
}

/**
 * Пример 2.3
 *
 * Приведем в пример основные операции работы с Flow
 */
@OptIn(FlowPreview::class)
private fun Example2_3() {
    val scope = CoroutineScope(Job())
    val firstFlow = flow {
        var number = 0
        while (true) emit(number++)
    }

    scope.launch {
        println("Возьмем 10 элементов: " + firstFlow.take(10).toList())
        println(
            "Можем еще раз спокойно взять 10 элементов, причем они будут такими же: " + firstFlow.take(
                10
            ).toList()
        )
        println("Можем вызвать 20 элементов: " + firstFlow.take(20).toList())
        val saverFlow = firstFlow.take(20)
        println(
            "Мы не сможем вызвать больше элементов, потому что сами ограничили последовательность 20-ю: " + saverFlow.take(
                30
            ).toList()
        )

        println("---------------------")
        println(
            "Операция drop, исключает заданные с начала элементы: " + firstFlow.take(20).drop(10)
                .toList()
        )

        println(
            "Операция filter, фильтрует элементы по условию: " + firstFlow.take(20)
                .filter { it > 9 }
                .toList()
        )

        println(
            "Операция filterNot, фильтрует элементы по условию (но тут наоборот они не должны совпадать с ним): " + firstFlow.take(
                20
            ).filterNot { it < 10 }.toList()
        )

        println(
            "Операция filterNotNull, работает с элементами, оставляя только не пустые): " + firstFlow.take(
                20
            ).map { if (it % 2 == 0) null else it }.filterNotNull().toList()
        )

        println(
            "Операция map, позволяет изменять downstream, приводить в любой удобный нам вид: " + firstFlow.take(
                20
            ).map { it.toDouble() }.toList()
        )

        println(
            "Операция onEach, позволяет проводить операцию над всеми приходящими элементами (например, логировать): " + firstFlow.take(
                5
            ).onEach { println("Логируем элемент $it") }.toList()
        )

        println(
            "Операция take, позволяет брать то кол-во элементов, которое нам нужно: " + firstFlow.take(
                16
            ).toList()
        )

        println("---- Существуют особые операции ----")
        val firstFlow = flow {
            var number = 0
            while (true) emit(number++)
        }

        val secondFlow = flow {
            var number = 0
            while (true) emit(number++)
        }

        val commonFlow = firstFlow.zip(secondFlow) { first, second ->
            first + second
        }
        println(
            "Операция zip позволяет объединить между собой две последовательности в одну ${
                commonFlow.take(
                    10
                ).toList()
            }"
        )
        println(
            "При работе с такой последовательностью мы будем получать значения сразу из двух ${
                commonFlow.take(
                    10
                ).toList()
            }"
        )

        println(
            "Операция flatMapConcat позволяет создавать группу потоков, а потом объединять их в один ${
                commonFlow.take(10).flatMapConcat { element ->
                    flowOf(element, element * 10)
                }.toList()
            }"
        )

        println(
            "FlatMap как нельзя лучше показывает разницу работу при перемещении операторв:\n ${
                commonFlow.take(10).flatMapConcat {
                    flowOf(it, it)
                }.toList()
            }\nи\n ${
                commonFlow.flatMapConcat {
                    flowOf(it, it)
                }.take(10).toList()
            }"
        )
    }
}

fun main() {
    Example2_3()
    while (true);
}