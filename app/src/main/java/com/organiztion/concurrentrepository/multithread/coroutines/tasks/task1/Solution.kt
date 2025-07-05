package com.organiztion.concurrentrepository.multithread.coroutines.tasks.task1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

suspend fun renderItems(messages: List<BrokerMessage>) {
    println("Render messages $messages")
    delay(5000)
    println("Render end")
}

val scope = CoroutineScope(Job())

class BrokerHandler(private val broker: Broker) {
    fun observeBroker() = flow {
        while (true) {
            delay((1000..1500).random().toLong())
            emit(broker.getMessagesByApi())
        }
    }
}

fun main() {
    val broker1 = BrokerHandler(Broker())
    val broker2 = BrokerHandler(Broker())
    val broker3 = BrokerHandler(Broker())

    scope.launch {
        combine(broker1.observeBroker(), broker2.observeBroker(), broker3.observeBroker()) {
            val (message1, message2, message3) = it
            val result = (message1 + message2 + message3).sortedBy { it.data }
            result
        }.collect {
            renderItems(it)
            println("Wait when message accept is will ready")
            delay(5000)
            println("Ready to accept message")
        }
    }

    while (scope.isActive);
}