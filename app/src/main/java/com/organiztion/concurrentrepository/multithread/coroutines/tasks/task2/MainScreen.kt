package com.organiztion.concurrentrepository.multithread.coroutines.tasks.task2

import com.organiztion.concurrentrepository.multithread.coroutines.tasks.task2.repository.DataApi
import com.organiztion.concurrentrepository.multithread.coroutines.tasks.task2.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Feature1(private val dataRepository: DataRepository) {
    val scope = CoroutineScope(Dispatchers.IO)
    fun execute() {
        scope.launch {
            dataRepository.update()
            //Do some work
            println("Feature1 work with ${dataRepository.getData()}")
        }
    }
}

class Feature2(private val dataRepository: DataRepository) {
    val scope = CoroutineScope(Dispatchers.IO)
    fun execute() {
        scope.launch {
            dataRepository.update()
            //Do some work
            println("Feature2 work with ${dataRepository.getData()}")
        }
    }
}

class Feature3(private val dataRepository: DataRepository) {
    val scope = CoroutineScope(Dispatchers.IO)
    fun execute() {
        scope.launch {
            dataRepository.update()
            //Do some work
            println("Feature3 work with ${dataRepository.getData()}")
        }
    }
}

fun main() {
    val repository = DataRepository(DataApi())
    val feature1 = Feature1(repository)
    val feature2 = Feature2(repository)
    val feature3 = Feature3(repository)

    while(true) {
        println("Doing some independent work")
        feature1.execute()
        feature2.execute()
        feature3.execute()
        Thread.sleep(5000)
    }
}