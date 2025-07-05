package com.organiztion.concurrentrepository.multithread.coroutines.tasks.task3

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.security.acl.Owner

/**
 * Пять безмолвных философов сидят вокруг круглого стола, перед каждым философом стоит тарелка спагетти.
 * На столе между каждой парой ближайших философов лежит по одной вилке.

 * Каждый философ может либо есть, либо размышлять.
 * Приём пищи не ограничен количеством оставшихся спагетти — подразумевается бесконечный запас.
 * Тем не менее, философ может есть только тогда, когда держит две вилки — взятую справа и слева.

 * Каждый философ может взять ближайшую вилку (если она доступна) или положить — если он уже держит её.
 * Взятие каждой вилки и возвращение её на стол являются раздельными действиями, которые должны выполняться одно за другим.
 */
fun main(): kotlin.Unit = runBlocking {
    val numPhilosophers = 5
    val forks = List(numPhilosophers) { Mutex() }
    val philosophers = List(numPhilosophers) { index ->
        val leftFork = forks[index]
        val rightFork = forks[(index + 1) % numPhilosophers]

        // Чтобы избежать deadlock, философы с четными номерами сначала берут левую вилку,
        // а с нечетными - правую
        val (firstFork, secondFork) = if (index % 2 == 0) {
            leftFork to rightFork
        } else {
            rightFork to leftFork
        }

        launch {
            val philosopherName = "Philosoph ${index + 1}"
            var eatCount = 0

            while (eatCount < 3) { // Каждый философ поест 3 раза
                think(philosopherName)
                eat(philosopherName, firstFork, secondFork)
                eatCount++
            }
            println("$philosopherName eat all")
        }
    }

    philosophers.joinAll()
}

suspend fun think(name: String) {
    println("$name think...")
    delay((1000L..2000L).random())
}

suspend fun eat(name: String, firstFork: Mutex, secondFork: Mutex) {
    println("$name want eat")

    // Блокируем обе вилки атомарно
    firstFork.withLock {
        secondFork.withLock {
            println("$name start eat")
            delay((500L..1500L).random())
            println("$name end eat")
        }
    }
}