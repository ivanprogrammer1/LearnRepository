package com.organiztion.concurrentrepository.multithread.flows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * Пример 1.1
 * Каналы в Kotlin бывают четырех видов, без буфера (RENDEZVOUS)
 * Пока мы не обработаем значение, следующе значение которое мы попытаемся отослать
 * заблокирует операцию.
 * Здесь второй send выполнится только через 5 секунд
 */
fun Example1_1() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>()
    scope.launch {
        println("Send first value")
        channel.send(5)
        println("Success send first value")
        println("Send second value")
        channel.send(1)
        println("Success send second value")
    }

    scope.launch {
        channel.receive()
        delay(5000)
        channel.receive()
    }
}

/**
 * Пример 1.2
 * Второй вид канала это CONFLATED (последнее приходящее значение затирается)
 * Здесь мы получим значение 1 без ожидания
 */
fun Example1_2() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>(CONFLATED)
    scope.launch {
        println("Send first value")
        channel.send(5)
        println("Success send first value")
        println("Send second value")
        channel.send(1)
        println("Success send second value")
    }

    scope.launch {
        println(channel.receive())
    }
}

/**
 * Пример 1.3
 * Третий вид канала это UNLIMITED (максимальный возможный буфер Int.MAX_VALUE)
 * Мы можем получать бесконечное число сообщений без ожидания, receive будет получать последнее
 * необработанное (как очередь)
 */
fun Example1_3() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>(UNLIMITED)
    scope.launch {
        println("Send first value")
        channel.send(5)
        println("Success send first value")
        println("Send second value")
        channel.send(1)
        println("Success send second value")
        channel.send(10)
    }

    scope.launch {
        println(channel.receive())
        println(channel.receive())
        println(channel.receive())
    }
}

/**
 * Пример 1.4
 * Четвертый вид канала это BUFFERED (буфер со стандартным размером (определяется JVM и по стандарту 64))
 * Аналогично третьему, только ограничен определенным размером
 */
fun Example1_4() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>(BUFFERED)
    scope.launch {
        println("Send first value")
        channel.send(5)
        println("Success send first value")
        println("Send second value")
        channel.send(1)
        println("Success send second value")
        channel.send(10)
    }

    scope.launch {
        println(channel.receive())
        println(channel.receive())
        println(channel.receive())
    }
}

/**
 * Пример 1.5
 * Также мы можем сами определять размер нашего буфера (от 0 до ...)
 * Здесь мы один три раза пытаемся отправить данные и один раз получить
 * В таком случае мы успешно отправили первый и второй запрос (второй хранится в буфере)
 * Третий же будет ожидать
 */
fun Example1_5() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>(1)
    scope.launch {
        println("Send first value")
        channel.send(5)
        println("Success send first value")
        println("Send second value")
        channel.send(1)
        println("Success send second value")
        println("Send third value")
        channel.send(10)
        println("We can't be here")
    }

    scope.launch {
        println(channel.receive())
    }
}

fun main() {
    Example1_5()
    while(true);
}