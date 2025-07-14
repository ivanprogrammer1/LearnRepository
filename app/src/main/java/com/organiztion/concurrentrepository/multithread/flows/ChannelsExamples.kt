package com.organiztion.concurrentrepository.multithread.flows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select


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

/**
 * Пример 4.1
 * Если нам важно отслеживать успех отправки или получения информации, то мы можем использовать обертку ChannelResult
 * Он позволяет получить значение, определить, успешно ли оно отправлено, закрыт ли канал и т.д.
 */
fun Example4_1() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>()
    scope.launch {
        val resultSend = channel.trySend(10)
        println("Success send: " + resultSend.isSuccess)
        println("Channel is close: " + resultSend.isClosed)
    }
    channel.close()
}

/**
 * Пример 4.2
 * Если мы не будем пользоваться этими методами то можем поймать ошибку
 */
fun Example4_2() {
    val scope = CoroutineScope(Job())
    val channel = Channel<Int>()
    scope.launch {
        val resultSend = channel.send(10)
    }
    channel.close()
}

/**
 * Пример 4.3
 * Также есть блокирующий метод, который под капотом использует runBlocking, но как по мне таким лучше не пользоваться
 */
fun Example4_3() {
    val channel = Channel<Int>()
    val resultSend = channel.trySendBlocking(run {
        println("Very long blocking operation")
        Thread.sleep(5000)
        5
    })
    channel.close()
}

/**
 * Пример 5.1
 * При работе с множеством каналов нам может понадобиться выбирать первое приходящее сообщение среди
 * всех остальных и как либо его обрабатывать. Для работы с подобным механизмом существует API Select
 * Если результат пришел, то другие каналы будут отменены
 */
fun Example5_1() {
    val scope = CoroutineScope(Job())
    scope.launch {
        val firstProducer = produce<Int> {
            delay(2000)
            send(5)
            println("Code wasn't reach")
        }
        val secondProducer = produce<Int> {
            delay(1000)
            send(10)
            println("Success reach")
        }

        // Здесь мы назначем обработчик, выбирающий итоговое значение
        val selectedValue = select {
            // Здесь представлены кэллбэки, сработает только тот который принимаем за результат
            firstProducer.onReceive {
                println("First producer on receive $it")
                it
            }
            secondProducer.onReceive {
                println("Second produce on receive $it")
                it
            }
        }

        println("Current value select $selectedValue")
    }
}

/**
 * Пример 5.2
 * Не только каналы, но и другие suspend-билдеры умеют работать вместе с Select
 * @see{<a href="https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.selects/select.html">Подробнее про все типы</a>}
 * При этом для каналов существует несколько видов обработчиков
 * onReceive - отвечает за потребление данных, которые были посланы через этот канал
 * onSend - отвечает за отправление данных через этот канал. Кто первый отправил (и что важно, обработал), тот и молодец
 */
fun Example5_2() {
    val scope = CoroutineScope(Job())
    scope.launch {
        val firstActor = actor<Int> {
            println("First actor wait")
            delay(5000)
            println("Receive for first ${receive()}")
        }
        val secondActor = actor<Int> {
            println("Second actor wait")
            delay(4000)
            println("Receive for second ${receive()}")
        }

        // Аналогично onReceive, мы в select получаем получаем итоговое значение, только еще вместе
        // с каналом, которому оно было отправлено
        val selectedValue = select {
            firstActor.onSend(5) {
                println("First actor success send $it")
                it
            }
            secondActor.onSend(10) {
                println("Second actor success send $it")
                it
            }
        }

        println("Current value select $selectedValue")
    }
}

/**
 * Пример 5.3
 * Однако, может быть ситуация когда при работе со списком каналов один из них закрывается, тогда
 * наш select также прокинет ошибку
 */
fun Example5_3() {
    val scope = CoroutineScope(Job())
    scope.launch {
        val firstActor = actor<Int> {
            println("First actor wait")
            delay(5000)
            println("Receive for first ${receive()}")
        }
        val secondActor = actor<Int> {
            println("Second actor wait")
            delay(4000)
            println("Receive for second ${receive()}")
        }

        launch {
            val selectedValue = select {
                firstActor.onSend(5) {
                    println("First actor success send $it")
                    it
                }
                secondActor.onSend(10) {
                    println("Second actor success send $it")
                    it
                }
            }

            println("Current value select $selectedValue")
        }
        firstActor.close()
    }
}

/**
 * Пример 5.4
 * Что интересно, ошибка имеено что прокидывается, а не распространяется как стандартные буилдеры async/launch
 */
fun Example5_4() {
    val scope = CoroutineScope(Job())
    scope.launch {
        val firstActor = actor<Int> {
            println("First actor wait")
            delay(5000)
            println("Receive for first ${receive()}")
        }
        val secondActor = actor<Int> {
            println("Second actor wait")
            delay(4000)
            println("Receive for second ${receive()}")
        }

        launch {
            try {
                val selectedValue = select {
                    firstActor.onSend(5) {
                        println("First actor success send $it")
                        it
                    }
                    secondActor.onSend(10) {
                        println("Second actor success send $it")
                        it
                    }
                }

                println("Current value select $selectedValue")
            } catch (exception: Exception) {
                println("We catch exception $exception")
            }
        }
        firstActor.close()
    }
}

/**
 * Пример 5.5
 * Еще один пример работы ошибки, демонстрирует работу не каналов/select, а launch, но показать надо
 */
fun Example5_5() {
    val scope = CoroutineScope(Job())
    scope.launch {
        val firstActor = actor<Int> {
            println("First actor wait")
            delay(5000)
            println("Receive for first ${receive()}")
        }
        val secondActor = actor<Int> {
            println("Second actor wait")
            delay(4000)
            println("Receive for second ${receive()}")
        }
        try {
            launch {

                val selectedValue = select {
                    firstActor.onSend(5) {
                        println("First actor success send $it")
                        it
                    }
                    secondActor.onSend(10) {
                        println("Second actor success send $it")
                        it
                    }
                }

                println("Current value select $selectedValue")
            }
        } catch (exception: Exception) {
            println("We never catch exception $exception")
        }

        firstActor.close()
    }
}

fun main() {
    Example5_5()
    while (true);
}