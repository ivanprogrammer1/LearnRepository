package com.organiztion.concurrentrepository.multithread.flows

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.yield


/**
 * Пример 1.1
 *
 * В Kotlin для работы с последовательностями есть несколько видов потоков.
 *
 * Первый вид - sequences. Он содержит все основные операции, которые используется и во Flow
 */
private fun Example1_1() {
    // Задачем список элементов
    val elements = sequenceOf(15, 20, 30, 40, 50)

    // Важен порядок назначения промежуточных операторов
    elements.filter { it > 30 }.map { it + 10 }.forEach {
        println("Здесь будут только элементы, которые больше 30: $it")
    }

    println("-------------")
    elements.map { it + 10 }.filter { it > 30 }.forEach {
        println("Здесь будут только элементы, которые больше 30: $it")
    }
}

/**
 * Пример 1.2
 *
 * Важно понимать, что sequences основан на генераторах. Сколько значений нам нужно, столько мы и можем вызввать (если последовательность содержит в себе бесконечный счетчик)
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

fun main() {
    Example1_3()
}