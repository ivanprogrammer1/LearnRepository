package com.organiztion.concurrentrepository.repository

data class ObjectDto(val id: Int, val name: String, val age: Int)

class DataApi {
    fun getData(): List<ObjectDto> {
        println("Start getData api")
        val list = listOf(
            ObjectDto(1, randomName(), randomNumber()),
            ObjectDto(2, randomName(), randomNumber()),
            ObjectDto(3, randomName(), randomNumber()),
            ObjectDto(4, randomName(), randomNumber()),
            ObjectDto(5, randomName(), randomNumber()),
            ObjectDto(6, randomName(), randomNumber()),
            ObjectDto(7, randomName(), randomNumber()),
            ObjectDto(8, randomName(), randomNumber()),
            ObjectDto(9, randomName(), randomNumber()),
        )
        Thread.sleep((5..10).random() * 1000L)
        println("End getData api")
        return list
    }
}

fun randomNumber() = (1..5000).random()
fun randomName() = listOf(
    "Ivan",
    "Kirill",
    "Oleg",
    "Dmitry",
    "Mikhail",
    "Aleksandr",
    "Ilya"
).random()