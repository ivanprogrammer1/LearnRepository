package com.organiztion.concurrentrepository.multithread.flows

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

/**
 * Пример 1.1
 *
 * SharedFlow - отвлетвление от flow, изначально являющийся горячим (то есть для его исполнения не нужен подписчик).
 *
 * Сама идея SharedFlow перенимает идеи каналов. У них также есть буфер, который позволяет сохранять значения.
 * Только если у канала есть лишь один подписчик, который работает с его значениями, то SharedFlow позволяет
 * повторять заданное кол-во значений всем новым подписчикам (задается через поле replay)
 */
private fun Example1_1() {
    val scope = CoroutineScope(Job())
    val sharedFlow = MutableSharedFlow<Int>(replay = 5)

    scope.launch {
        sharedFlow.emitAll(flowOf(1, 2, 3, 4, 5))

        launch {
            sharedFlow.collect {
                println("Поток 1 обработает значение $it")
            }
        }

        launch {
            sharedFlow.collect {
                println("Поток 2 обработает значение $it")
            }
        }
    }
}

/**
 * Пример 1.2
 *
 * С каналами же такая ситуация невозможна.
 */
private fun Example1_2() {
    val scope = CoroutineScope(Job())
    val correctChannel = Channel<Int>(capacity = 5)

    scope.launch {
        correctChannel.send(1)
        correctChannel.send(2)
        correctChannel.send(3)
        correctChannel.send(4)
        correctChannel.send(5)

        launch {
            println("Поток 1 обработает значение ${correctChannel.receive()}")
            println("Поток 1 обработает значение ${correctChannel.receive()}")
            println("Поток 1 обработает значение ${correctChannel.receive()}")
            println("Поток 1 обработает значение ${correctChannel.receive()}")
            println("Поток 1 обработает значение ${correctChannel.receive()}")
        }

        launch {
            println("Поток 2 обработает значение ${correctChannel.receive()}")
            println("Поток 2 обработает значение ${correctChannel.receive()}")
            println("Поток 2 обработает значение ${correctChannel.receive()}")
            println("Поток 2 обработает значение ${correctChannel.receive()}")
            println("Поток 2 обработает значение ${correctChannel.receive()}")
        }
    }
}

/**
 * Пример 1.3
 *
 * Для построениея SharedFlow существует несколько конструкторов
 */
private fun Example1_3() {
    val scope = CoroutineScope(Job())
    // Прямой конструктор
    val sharedFlowFirst = MutableSharedFlow<Int>()

    // Установка конструктора через shareIn (переводит холодный поток в горячий, то есть при работе с потоком
    // данные будут буферизироваться и передаваться подписчикам)
    val sharedSecondFlow = flowOf(1, 2, 3, 4, 5).shareIn(scope, SharingStarted.Eagerly)
}

/**
 * Пример 1.4
 *
 * При работе с SharedFlow важно учесть какие параметры задаются (так как буфер делится на два вида)
 */
private fun Example1_4() {
    // Здесь мы объявляем, что наш буфер состоит из 5 элементов, котоорые будут повторяться для наших новых подписчиков
    val sharedFlowFirst = MutableSharedFlow<Int>(replay = 5)

    // Здесь мы объявляем говорим системе, что наш буфер содержит размер 5 и если мы не будем успевать обработывать последние 5 элементов,
    // они будут сохраняться у нас в буфере
    val sharedFlowSecond = MutableSharedFlow<Int>(extraBufferCapacity = 5)

    // Здесь мы объединяем работу первого и второго буфера, в таком случае размер буфера будет состоять из 10 элементов,
    // Мы говорим системе, что мы обязательно должны повторить 5 предыдуших элементов для новых подписчиков
    // При этом если мы не будем успевать обработать, то они будут накапливаться через extraBufferCapacity
    val sharedFlowThird = MutableSharedFlow<Int>(replay = 5, extraBufferCapacity = 5)
}

/**
 * Пример 2.1
 *
 * При работе с буфером важно знать основные особенности переполнения
 * Рассмотрим пример работы с буфером replay, когда мы не вызываем collect
 * Если у нас нет подписчика, то мы можем успешно присылать любое кол-во значений для нашей последовательности,
 * использоваться будут только новые
 */
private fun Example2_1() {
    val scope = CoroutineScope(Job())
    // Здесь мы объявляем, что наш буфер состоит из 5 элементов, котоорые будут повторяться для наших новых подписчиков
    val sharedFlowFirst = MutableSharedFlow<Int>(replay = 5)

    scope.launch {
        println("Отправляем значения")
        sharedFlowFirst.emit(5)
        sharedFlowFirst.emit(6)
        sharedFlowFirst.emit(7)
        sharedFlowFirst.emit(8)
        sharedFlowFirst.emit(9)
        println("Успешно отправили значения")
        sharedFlowFirst.emit(11)
        sharedFlowFirst.emit(12)
        sharedFlowFirst.emit(13)
        sharedFlowFirst.emit(14)
        sharedFlowFirst.emit(15)
        println("Все равно достигнем, так как буфер не переполнен")
    }
}

/**
 * Пример 2.2
 *
 * Если же подписчки присутствует, но при этом слишком долго обрабатывает операции, то мы будем ожидать каждую отправку сообщения,
 * не входящего в буфер
 */
private fun Example2_2() {
    val scope = CoroutineScope(Job())
    // Здесь мы объявляем, что наш буфер состоит из 5 элементов, котоорые будут повторяться для наших новых подписчиков
    val sharedFlowFirst = MutableSharedFlow<Int>(replay = 5)

    scope.launch {
        // Формируем долгого подписчика, чтобы наши emit значения не уходили в пустоту
        sharedFlowFirst.collect {
            delay(10.hours)
        }
    }

    scope.launch {
        println("Отправляем значения")
        sharedFlowFirst.emit(5)
        sharedFlowFirst.emit(6)
        sharedFlowFirst.emit(7)
        sharedFlowFirst.emit(8)
        sharedFlowFirst.emit(9)
        sharedFlowFirst.emit(11)
        println("Успешно отправили значения") // Здесь на один больше, так как подписчик изначально получает первое при подписке
        sharedFlowFirst.emit(12)
        println("Придется очень долго ждать....")
        sharedFlowFirst.emit(13)
        sharedFlowFirst.emit(14)
        sharedFlowFirst.emit(15)
        println("Точно не дойдем")
    }

    scope.launch {
        delay(1000)
        // При этом это никак не тормозит работу другого подписчика. Ну, кроме ожидания следующего элемента
        sharedFlowFirst.collect {
            println("А мы все быстро получим $it")
        }
    }
}

/**
 * Пример 2.3
 *
 * Аналогичное поведение завязано и на extraBufferCapacity. Только здесь мы уже не будем получать
 * старые значения
 */
private fun Example2_3() {
    val scope = CoroutineScope(Job())
    // Здесь мы объявляем, что наш буфер состоит из 5 элементов, котоорые будут повторяться для наших новых подписчиков
    val sharedFlowFirst = MutableSharedFlow<Int>(extraBufferCapacity = 5)

    scope.launch {
        // Имитируем долгую работу
        sharedFlowFirst.collect {
            delay(10.hours)
        }
    }

    scope.launch {
        println("Отправляем значения")
        sharedFlowFirst.emit(5)
        sharedFlowFirst.emit(6)
        sharedFlowFirst.emit(7)
        sharedFlowFirst.emit(8)
        sharedFlowFirst.emit(9)
        sharedFlowFirst.emit(11)
        println("Успешно отправили значения") // Здесь на один больше, так как подписчик изначально получает первое при подписке
        sharedFlowFirst.emit(12)
        println("Придется очень долго ждать....")
        sharedFlowFirst.emit(13)
        sharedFlowFirst.emit(14)
        sharedFlowFirst.emit(15)
        println("Точно не дойдем")
    }

    // При этом, что интересно. Мы продолжаем получать последние значения, которые у нас хранятся в буфере, пусть он даже и не replay
    scope.launch {
        delay(1000)
        sharedFlowFirst.collect {
            println("А мы все быстро получим $it")
        }
    }
}

/**
 * Пример 2.4
 *
 * Попробуем сымитировать работу
 */
private fun Example2_4() {

}

/**
 * Пример 3.1
 *
 * Как уже было сказано, shareIn переводит холодный поток в горячий. Рассмотрим, как оно влияет на работу
 * при разных конфигурациях последовательности
 */
private fun Example3_1() {
    val scope = CoroutineScope(Job())

    // Переводим холодный поток в горячий, так как мы запустили его сразу, то он будет выполняться, не ожидая подписчика
    val firstFlow = flow {
        while (true) {
            println("Работает!")
            delay(5000)
            emit(10)
        }
    }.shareIn(scope, SharingStarted.Eagerly, replay = 1)
}

/**
 * Пример 3.2
 *
 * Что будет, если у нас долгий подписчик и произойдет переполнение буфера?
 */
private fun Example3_2() {
    val scope = CoroutineScope(Job())

    // Переводим холодный поток в горячий, так как мы запустили его сразу, то он будет выполняться, не ожидая подписчика
    val firstFlow = flow {
        var number = 0
        while (true) {
            println("Работает!")
            delay(1000)
            emit(number++)
        }
    }.shareIn(scope, SharingStarted.Eagerly, replay = 1)

    // По флоу вывода мы можем увидеть что операция emit работает достаточно долго независимо от нашего подписчика.
    // Это связано с тем, что система обладает начальным буфером, который назначется по умолчанию
    scope.launch {
        firstFlow.collect {
            println("Value $it")
            delay(5000)
        }
    }
}

/**
 * Пример 3.3
 *
 * Несмотря на то что в shareIn мы не можем задать буфер (кроме replay), мы можем использовать
 * специальный extension функции. Например, buffer
 */
private fun Example3_3() {
    val scope = CoroutineScope(Job())

    // Ограничимся только нашим буфером, теперь он не будет работать так резво как раньше
    val firstFlow = flow {
        var number = 0
        while (true) {
            println("Работает!")
            delay(1000)
            emit(number++)
        }
    }.buffer(0).shareIn(scope, SharingStarted.Eagerly, replay = 1)

    scope.launch {
        launch {
            firstFlow.collect {
                println("Work first flow $it")
                delay(5000)
            }
        }
    }
}


/**
 * Пример 3.4
 *
 * Также приведем пример с conflate.
 * Он устанавливает размер буфера на 1, если предыдущее значение еще не обработано, то заменяется новым
 */
private fun Example3_4() {
    val scope = CoroutineScope(Job())

    val secondFlow = flow {
        var number = 0
        while (true) {
            println("Работает!")
            delay(1000)
            emit(number++)
        }
    }.conflate().shareIn(scope, SharingStarted.Eagerly, replay = 1)

    scope.launch {
        launch {
            secondFlow.collect {
                println("Work second flow $it")
                delay(5000)
            }
        }
    }
}

/**
 * Пример 4.1
 *
 * Разница работы при назначении подписчиков.
 * Если у нас изначально нет подписчиков и буфера, то мы ничего не вернем
 */
private fun Example4_1() {
    val firstScope = CoroutineScope(Job())
    val secondScope = CoroutineScope(Job())

    val basicFlow = MutableSharedFlow<Int>()

    firstScope.launch {
        basicFlow.emit(1)
        basicFlow.emit(2)
        basicFlow.emit(3)
        basicFlow.emit(4)
    }

    firstScope.launch {
        delay(1000) // Чтобы наверняка после
        basicFlow.collect {
            println("Work first flow $it")
        }
    }

    secondScope.launch {
        delay(1000) // Чтобы наверняка после
        basicFlow.collect {
            println("Work second flow $it")
        }
    }
}

/**
 * Пример 4.2
 *
 * Здесь же мы будем принимать все значения
 */
private fun Example4_2() {
    val firstScope = CoroutineScope(Job())
    val secondScope = CoroutineScope(Job())

    val basicFlow = MutableSharedFlow<Int>()

    firstScope.launch {
        basicFlow.collect {
            println("Work first flow $it")
        }
    }

    secondScope.launch {
        basicFlow.collect {
            println("Work second flow $it")
        }
    }

    firstScope.launch {
        delay(1000) // Чтобы наверняка после
        basicFlow.emit(1)
        basicFlow.emit(2)
        basicFlow.emit(3)
        basicFlow.emit(4)
    }
}

/**
 * Пример 5.1
 *
 * Попробуем составить сложный механизм многоступенчатой буферизации. Посмотрим, как он будет работать
 */
private fun Example5_1() {
    val firstScope = CoroutineScope(Job())

    // Например, у нас есть стандартная последовательность, которая должна повторять 10 элементов своим новым подписчикам
    val basicFlow = MutableSharedFlow<Int>(replay = 10)

    // Здесь мы задаем новую последовательность, где мы должны повторять только последний элемент
    val flowWithSmallReplay = basicFlow.shareIn(firstScope, SharingStarted.Eagerly, replay = 1)

    firstScope.launch {
        basicFlow.emit(1)
        basicFlow.emit(2)
        basicFlow.emit(3)
        basicFlow.emit(4)
        basicFlow.emit(5)
        basicFlow.emit(6)
    }

    firstScope.launch {
        delay(1000)
        flowWithSmallReplay.collect {
            println("Work first flow $it")
        }
    }
}

/**
 * Пример 5.2
 *
 * Важно учесть стратегию нашей подписки. Если речь идет об Eagerly, то мы сразу же подпишемся на нашу основную последовательнсть,
 * благодаря чему уже наша отвлетвленная будет считать что прошлые значения действительно прошлые.
 * Если же мы применяем другие стратегии (Lazily, WhileSubscribed), то при подписке наша последовательность принимает значения с буфера основной
 * последовательности как новые значения и, поэтому, при запуске подписчика отображает их
 */
private fun Example5_2() {
    val firstScope = CoroutineScope(Job())

    val basicFlow = MutableSharedFlow<Int>(replay = 10)

    // Здесь мы задаем новую последовательность, где мы должны повторять только последний элемент
    val flowWithSmallReplay = basicFlow.shareIn(firstScope, SharingStarted.Lazily, replay = 1)

    firstScope.launch {
        basicFlow.emit(1)
        basicFlow.emit(2)
        basicFlow.emit(3)
        basicFlow.emit(4)
        basicFlow.emit(5)
        basicFlow.emit(6)
    }

    firstScope.launch {
        delay(1000)
        flowWithSmallReplay.collect {
            println("Work first flow $it")
        }
    }
}

/**
 * Пример 5.3
 *
 * Здесь мы уже работаем с обычным буфером. Проверим, как у нас происходит обработка принятие значений,
 * если подбуфер слишком долго обрабатывается
 */
private fun Example5_3() {
    val firstScope = CoroutineScope(Job())

    val basicFlow = MutableSharedFlow<Int>(extraBufferCapacity = 3)

    val flowWithSmallReplay = basicFlow.buffer(1)

    firstScope.launch {
        flowWithSmallReplay.collect {
            println("Work first flow small replay $it")
            delay(5000)
        }
    }

    // Операции все пройдут успешно, так как они затрагивают работу основного буфера, а не другого
    firstScope.launch {
        delay(1000)
        basicFlow.emit(1)
        println("Success emit 1")
        delay(500)
        basicFlow.emit(2)
        println("Success emit 2")
        delay(500)
        basicFlow.emit(3)
        println("Success emit 3")
        delay(500)
        basicFlow.emit(4)
        println("Success emit 4")
        delay(500)
        basicFlow.emit(5)
        println("Success emit 5")
        delay(500)
        basicFlow.emit(6)
        println("Success emit 6")
    }
}

/**
 * Пример 5.4
 *
 * При этом если у нас существует проблема с долгой обработкой главной последовательности, то подпотоку придется ожидать
 */
private fun Example5_4() {
    val firstScope = CoroutineScope(Job())

    val basicFlow = MutableSharedFlow<Int>(extraBufferCapacity = 3)

    val flowWithSmallReplay = basicFlow.buffer(1)

    firstScope.launch {
        launch {
            basicFlow.collect {
                delay(10000)
            }
        }
        launch {
            flowWithSmallReplay.collect {
                println("Work first flow small replay $it")
                delay(1000)
            }
        }
    }

    // Операции все пройдут успешно, так как они затрагивают работу основного буфера, а не другого
    firstScope.launch {
        delay(1000)
        basicFlow.emit(1)
        println("Success emit 1")
        delay(500)
        basicFlow.emit(2)
        println("Success emit 2")
        delay(500)
        basicFlow.emit(3)
        println("Success emit 3")
        delay(500)
        basicFlow.emit(4)
        println("Success emit 4")
        delay(500)
        basicFlow.emit(5)
        println("Success emit 5")
        delay(500)
        basicFlow.emit(6)
        println("Success emit 6")
    }
}

/**
 * Пример 5.5
 *
 * Посмотрим, что произойдет при смене стратегии переполнении буфера
 *
 * Получается, что мы будем использовать последний элемент, который к нам пришел.
 */
private fun Example5_5() {
    val firstScope = CoroutineScope(Job())

    val basicFlow = MutableSharedFlow<Int>(extraBufferCapacity = 3) // Здесь у нас остается Suspend

    val flowWithSmallReplay = basicFlow.buffer(1, onBufferOverflow = BufferOverflow.DROP_OLDEST) // Здесь испольузем DROP_OLDEST

    firstScope.launch {
        launch {
            flowWithSmallReplay.collect {
                println("Work first flow small replay $it")
                delay(5000)
            }
        }
    }

    // Операции все пройдут успешно, так как они затрагивают работу основного буфера, а не другого
    firstScope.launch {
        delay(1000)
        basicFlow.emit(1)
        println("Success emit 1")
        delay(500)
        basicFlow.emit(2)
        println("Success emit 2")
        delay(500)
        basicFlow.emit(3)
        println("Success emit 3")
        delay(500)
        basicFlow.emit(4)
        println("Success emit 4")
        delay(500)
        basicFlow.emit(5)
        println("Success emit 5")
        delay(500)
        basicFlow.emit(6)
        println("Success emit 6")
    }
}

fun main() {
    Example5_5()
    while (true);
}