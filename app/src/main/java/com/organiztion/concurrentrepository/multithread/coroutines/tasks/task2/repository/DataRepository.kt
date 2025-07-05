package com.organiztion.concurrentrepository.multithread.coroutines.tasks.task2.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * У нас есть репозиторий, от которого нам нужно получать данные и как-то их обрабатывать
 * Потребителями информации являются три фичи, которые запрашивают информацию с разными промежутками
 * Если наши фичи хотят получить информацию одновременно друг с другом, мы должны дать выполниться
 * лишь одному из запросов, а уже его результат распространить на всех остальных потребителей
 */
class DataRepository(private val dataApi: DataApi) {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex(locked = false)
    private var data: List<ObjectDto>? = null

    suspend fun getData(): List<ObjectDto>? {
        job?.join()
        return data
    }

    suspend fun update() {
        mutex.withLock {
            if (job != null) return
            job = scope.launch {
                data = dataApi.getData()
            }
            job?.invokeOnCompletion {
                job = null
            }
        }
    }
}