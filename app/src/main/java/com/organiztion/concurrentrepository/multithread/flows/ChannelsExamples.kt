package com.organiztion.concurrentrepository.multithread.flows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
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

/**
 * Мы можем определять поведение канала не завися от буфера
 * Так, канал может работать в трех режимах
 */

/**
 * Пример 2.1
 * Канал по стандарту работает в режиме Suspend
 * Это означает что если в буфере нет свободного места, то мы будем ожидать пока не обработаем его
 * на стороне получателя
 */
fun Example2_1() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>(onBufferOverflow = BufferOverflow.SUSPEND)
    scope.launch {
        println("Send first value")
        channel.send(5)
        println("This code never reach")
    }
}

/**
 * Пример 2.2
 * Режим DROP_OLDEST
 * Если в буфере не будет хватать места, то мы удалим последний элемент из списка
 */
fun Example2_2() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>(onBufferOverflow = BufferOverflow.DROP_OLDEST)
    scope.launch {
        channel.send(5)
        channel.send(10)
    }
    scope.launch {
        println(channel.receive()) // here will be 10
    }
}

/**
 * Пример 2.3
 * Режим DROP_LATEST
 * Если в буфере не будет хватать места, то мы удалим первый элемент в очереди
 */
fun Example2_3() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>(onBufferOverflow = BufferOverflow.DROP_LATEST)
    scope.launch {
        channel.send(5)
        channel.send(10)
    }
    scope.launch {
        println(channel.receive()) // here will be 5
    }
}

/**
 * Пример 3.1
 * Channel при отправке или получении сообщений может закрываться (либо мы сами закрыли наш канал через cancel, либо наша корутина была закрыта)
 * Также может возникнуть ситуация, когда наш элемент не дошел до получателя из-за DROP_LATEST/DROP_OLDEST
 * Чтобы отработать эту ситуациию существует метод onUndeliveredElement
 * Если в буфере не будет хватать места, то мы удалим первый элемент в очереди
 * В нашем примере мы храним буфер из 5 элементов. Так как мы закрываем канал до того как обработали значения, то они передадутся к нам в onUndeliveredElement
 */
fun Example3_1() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>(
        capacity = 5,
        onUndeliveredElement = {
            println("Undelivered element $it")
        }
    )
    scope.launch {
        channel.send(5)
        channel.send(10)
        channel.send(15)
        channel.send(20)
        channel.send(25)
    }
    scope.launch {
        delay(1000) // Подождем пока все значения придут в буфер
        channel.cancel()
    }
}


/**
 * Пример 3.2
 * Также примером может служить DROP_OLDEST, даже если мы не закрыли канал необработанное значение
 * все равно придет к нам
 * Но нужно явно учесть, что это будет работать только если размер нашего буфера больше 0
 */
fun Example3_2() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>(
        capacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        onUndeliveredElement = {
            println("Undelivered element $it")
        }
    )
    scope.launch {
        channel.send(5)
        channel.send(10)
        channel.send(15)
        channel.send(20)
        channel.send(25)
    }
}

/**
 * Пример 3.3
 * Этот пример работать уже не будет (мы не храним никакие элементы в буфере и канал считает
 * что и недоставленных элементов нет
 */
fun Example3_3() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        onUndeliveredElement = {
            println("Undelivered element $it")
        }
    )
    scope.launch {
        channel.send(5)
        channel.send(10)
        channel.send(15)
        channel.send(20)
        channel.send(25)
    }
}

fun main() {
    Example3_3()
    while (true);
}