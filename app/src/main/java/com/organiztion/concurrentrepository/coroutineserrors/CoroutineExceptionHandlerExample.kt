package com.organiztion.concurrentrepository.coroutineserrors

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.suspendCoroutine

/*
Главный механизм обработки ошибок в Kotlin coroutines - CoroutinesExceptionHandler,
для взаимодействия с механизмом нужно знать его особенность - его необходимо вставлять только
наверх, при создании Scope или при вызове scope верхнего уровня.
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

/*
Поведение аналогичное первому примеру
 */

fun Example2() {
    val scope = CoroutineScope(Job())
    scope.launch(CoroutineExceptionHandler { _, _ ->
        println("Catch error")
    }) {
        println("We throw error and it will catch our handler")
        throw Exception("Our exception")
    }
}

/*
Здесь будет поймана только первая ошибка
 */

fun Example3() {
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

/*
Здесь будут пойманы все ошибки
 */

fun Example4() {
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

/*
Работа с try-catch.
Работа с try-catch механизмом в корутинах возможна только если try-catch внутри корутины
(но тогда ломается structured concurrency) или в иных ситуациях
 */

fun Example5() {
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

/*
Здесь мы ошибку не поймаем
 */

fun Example6() {
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

/*
Однако если речь идет об отдельных scope-генератора (supervisorScope/coroutineScope),
то мы ее сможем поймать
 */

fun Example7() {
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

/*
Тоже самое можно сказать и об suspendCoroutine/suspendCancellableCoroutine
 */

fun Example8() {
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

/*
Разная логика испоьзования coroutineScope и supervisorScope.
Оба наследуют контекст вызывающей их корутины, не распространяют наверх а пробрасывают
исключения. При этом у каждой есть особенности
 */

/*
При ошибке в одной из дочерних корутин отменяются и все остальные
 */
fun Example10() {
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

/*
Для supervisorScope мы можем установить собственный обработчик ошибок
 */
fun Example11() {
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

/*
При ошибке в одной из корутин другая продолжает свою работу
 */

fun Example12() {
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

fun main() {
    Example12()
    while (true);
}