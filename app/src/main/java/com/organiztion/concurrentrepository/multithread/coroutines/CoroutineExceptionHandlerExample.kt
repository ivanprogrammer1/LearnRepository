package com.organiztion.concurrentrepository.multithread.coroutines

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.suspendCoroutine

/**
Главный механизм обработки ошибок в Kotlin coroutines - CoroutinesExceptionHandler,
для взаимодействия с механизмом нужно знать его особенность - его необходимо вставлять только
наверх, при создании Scope или при вызове scope верхнего уровня.
 */


/**
 * Пример 1, обработка ошибки
 */
fun Example1() {
    val scope = CoroutineScope(CoroutineExceptionHandler { _, _ ->
        println("Catch error")
    })
    scope.launch {
        println("We throw error and it will catch our handler")
        throw Exception("Our exception")
    }
}

/**
 * Пример 1.1, вынесли обработку ошибки на уровень верхней корутины
 */
fun Example1_1() {
    val scope = CoroutineScope(Job())
    scope.launch(CoroutineExceptionHandler { _, _ ->
        println("Catch error")
    }) {
        println("We throw error and it will catch our handler")
        throw Exception("Our exception")
    }
}

/**
 * Пример 1.2, обработка ошибок зависит от места размещения обработчика. Здесь будет поймана
 * только первая ошибка
 */
fun Example1_2() {
    val scope = CoroutineScope(Job())
    scope.launch(CoroutineExceptionHandler { _, thr ->
        println("Catch error $thr")
    }) {
        println("We throw error and it will catch our handler")
        throw Exception("Our exception")
    }
    scope.launch {
        println("We throw error 2 and it will catch our handler")
        throw Exception("Our exception 2")
    }
}

/**
 * Пример 1.3, здесь будут пойманы все ошибки
 */
fun Example1_3() {
    val scope = CoroutineScope(Job() + CoroutineExceptionHandler { _, thr ->
        println("Catch error $thr")
    })
    scope.launch {
        println("We throw error and it will catch our handler")
        throw Exception("Our exception")
    }
    scope.launch {
        println("We throw error 2 and it will catch our handler")
        throw Exception("Our exception 2")
    }
}

/**
 * Пример 2
 * Работа с try-catch.
 * Работа с try-catch механизмом в корутинах возможна только если try-catch внутри корутины
 * (но тогда ломается structured concurrency) или в других конкретных ситуациях,
 * описанных ниже
 */
fun Example2() {
    val scope = CoroutineScope(Job())
    scope.launch {
        launch {
            try {
                println("We throw error and it will catch our handler")
                throw Exception("Our exception")
            } catch (exception: Exception) {
                println("We success catch error $exception")
            }
        }
    }
}

/**
 * Пример 2.1
 * Так как ошибка распространяется наверх, а не пробрасывается, то попытка ее поймать здесь
 * не принесет успеха
 */
fun Example2_1() {
    val scope = CoroutineScope(Job())
    scope.launch {
        try {
            launch {
                println("We throw error and it will catch our handler")
                throw Exception("Our exception")
            }
        } catch (exception: Exception) {
            println("We can't catch this $exception")
        }
    }
}

/**
 * Пример 2.2
 * Однако если речь идет об отдельных scope-генераторах (supervisorScope/coroutineScope),
 * то мы ее сможем поймать, это связано с тем что они являются отдельными механизмами,
 * которые принимают контекст родительской корутины, но обрабатывают результат сами
 */
fun Example2_2() {
    val scope = CoroutineScope(Job())
    scope.launch {
        try {
            coroutineScope {
                println("We throw error and it will catch our handler")
                throw Exception("Our exception1")
            }
        } catch (exception: Exception) {
            println("We success catch error $exception")
        }
        try {
            supervisorScope {
                println("We throw error and it will catch our handler")
                throw Exception("Our exception2")
            }
        } catch (exception: Exception) {
            println("We success catch error $exception")
        }
    }
}

/**
 * Пример 2.3
 * Тоже самое можно сказать и об suspendCoroutine/suspendCancellableCoroutine
 * Они принимают внешний контекст, но обрабатывают результат сами
 */
fun Example2_3() {
    val scope = CoroutineScope(Job())
    scope.launch {
        try {
            suspendCancellableCoroutine {
                println("We throw error and it will catch our handler")
                throw Exception("Our exception1")
            }
        } catch (exception: Exception) {
            println("We success catch error $exception")
        }

        try {
            suspendCoroutine {
                println("We throw error and it will catch our handler")
                throw Exception("Our exception2")
            }
        } catch (exception: Exception) {
            println("We success catch error $exception")
        }
    }
}

/**
 * Разная логика испоьзования coroutineScope и supervisorScope.
 * Оба наследуют контекст вызывающей их корутины, не распространяют наверх а пробрасывают
 * исключения. При этом у каждой есть особенности
 */

/**
 * Пример 3_1, coroutineScope
 * При ошибке в одной из дочерних корутин отменяются и все остальные
 */
fun Example3_1() {
    val scope = CoroutineScope(Job())
    scope.launch {
        coroutineScope {
            launch {
                println("Start first work")
                delay(5000)
                println("End first work")
            }
            launch {
                println("Start second work")
                throw Exception("Exception")
                println("End second work")
            }
        }
    }
}

/**
 * Пример 3_2, coroutineScope
 * Обработчик ошибок в coroutineScope не работает (придется ловить через внешний try/catch,
 * нарушая structured concurrency или через ставить обработчик в самом начале
 */
fun Example3_2() {
    val scope = CoroutineScope(Job())
    scope.launch {
        coroutineScope {
            launch {
                println("Start first work")
                delay(5000)
                println("End first work")
            }
            launch(CoroutineExceptionHandler { _, _ -> println("Throw error") }) {
                println("Start second work")
                throw Exception("Exception")
                println("End second work")
            }
        }
    }
}


/**
 * Пример 3_3, supervisorScope
 * Для supervisorScope мы же можем установить обработчик ошибок на верхних вызываемых ею корутинах
 */
fun Example3_3() {
    val scope = CoroutineScope(Job())
    scope.launch {
        supervisorScope {
            launch(CoroutineExceptionHandler { _, _ ->
                println("Catch error")
            }) {
                println("We send exception")
                throw Exception("Exception")
            }
        }
    }
}

/**
 * Пример 3_4, supervisorScope
 * При этом этот прибер не будет работать и ошибку мы не поймаем, только для верхнеуровневых
 */
fun Example3_4() {
    val scope = CoroutineScope(Job())
    scope.launch {
        supervisorScope {
            launch {
                launch(CoroutineExceptionHandler { _, _ ->
                    println("Catch error")
                }) {
                    println("We send exception")
                    throw Exception("Exception")
                }
            }
        }
    }
}

/**
 * Пример 3_5, supervisorScope
 * Если одна из корутин сломалась, то другая продолжит свою работу. И не важно,
 * был ли у нас обработчик ошибок у сломанной или нет
 */
fun Example3_5() {
    val scope = CoroutineScope(Job())
    scope.launch(CoroutineExceptionHandler { _, _ ->
        println("Catch error 1")
    }) {
        supervisorScope {
            launch {
                println("We send exception")
                throw Exception("Exception1")
            }

            launch {
                println("Start second work")
                delay(3000)
                println("End second work")
            }
        }
    }
}

/**
 * Пример 4_1
 * Как и launch, async имеет собственные особенности при работе
 * Так, Async умеет сохранять результат работы, благодаря чему мы можем его получить с помощью await
 */
fun Example4_1() {
    val scope = CoroutineScope(Job())
    scope.launch {
        val asyncJob = async {
            delay(1000)
            return@async 5
        }
        asyncJob.await() // Здесь будет 5
    }
}

/**
 * Пример 4_2
 * Async умеет хранить результат своей работы, при этом есть большая разница между верхнеуровнеыми
 * async и стандартными. Так, если в результате работы у верхнеуровневой возникнет ошибка, то ее
 * результат не распространится, а будет проброшен при вызове await
 */
fun Example4_2() {
    val scope = CoroutineScope(Job())
    scope.launch {
        supervisorScope {
            val asyncJob = async {
                delay(1000)
                throw Exception("My exception")
            }
        }
    }
}

/**
 * Пример 4_3
 * Благодаря методу await получим ошибку
 */
fun Example4_3() {
    val scope = CoroutineScope(Job())
    scope.launch {
        supervisorScope {
            val asyncJob = async {
                delay(1000)
                throw Exception("My exception")
            }
            asyncJob.await()
        }
    }
}

/**
 * Пример 5.1 Иногда при завершении scope/job нам необходимо завершить какую-либо операцию
 * Однако если scope/job отменена, то операция часто не может быть продолжена, из-за чего
 * возникает проблема.
 */
fun Example5_1() {
    val scope = CoroutineScope(Job())
    scope.launch {
        try {
            println("Trying work")
        } catch (exception: Exception) {
            println("Our exception $exception")
        } finally {
            println("Trying finish operation")
            delay(5000)
            println("Success finish operation") // Мы не дойдем этого участка
        }
    }
    scope.cancel()
}

/**
 * Пример 5.2 Для таких ситуаций мы можем использовать NonCancellable, чья работа продолжается все
 * время пока не дойдет до момента окончания. Важное уточнение, она может использоваться только
 * совместно с withContext, любое другое использование может привести к разрыву связей между
 * родителем-ребенком
 */
fun Example5_2() {
    val scope = CoroutineScope(Job())
    scope.launch {
        try {
            println("Trying work")
        } catch (exception: Exception) {
            println("Our exception $exception")
        } finally {
            withContext(NonCancellable) {
                println("Trying finish operation")
                delay(5000)
                println("Success finish operation")
            }
        }
    }
    scope.cancel()
}

fun main() {
    Example5_1()
    while (true);
}