package com.organiztion.concurrentrepository.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
            if(job != null) return
            job = scope.launch {
                data = dataApi.getData()
            }
            job?.invokeOnCompletion {
                job = null
            }
        }
    }
}