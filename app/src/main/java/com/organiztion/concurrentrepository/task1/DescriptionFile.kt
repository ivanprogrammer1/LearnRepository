package com.organiztion.concurrentrepository.task1

import com.organiztion.concurrentrepository.repository.randomNumber
import java.time.LocalDate
import kotlin.random.Random

/*
Принимаем входной канал со структурами, содержащими информацию и случайные даты.
Они приходят от нескольких Корутин. Сортируем их по порядку (от самой ранней к самой поздней) и
используем другой канал для отправки структур другой Корутине.
 */

/*
У нас есть доступ до трех брокеров сообщений, нам необходимо получать данные с каждого из брокера,
сортировать и располагать на экране. Рендер на экране занимает большое время, нам нужно это время
ожидать и не принимать новые значения, а после успешного рендера сделать задержку в 5 секунд

*/

data class BrokerMessage(val data: LocalDate, val id: Int)

fun getRandomLocalDate(startYear: Int, endYear: Int): LocalDate {
    val randomYear = Random.nextInt(startYear, endYear + 1)

    // Determine the maximum day of the year for the random year (considering leap years)
    val maxDayOfYear = if (LocalDate.of(randomYear, 1, 1).isLeapYear()) 366 else 365

    val randomDayOfYear = Random.nextInt(1, maxDayOfYear + 1)

    return LocalDate.of(randomYear, 1, 1).plusDays((randomDayOfYear - 1).toLong())
}

class Broker() {
    fun getMessagesByApi(): List<BrokerMessage> {
        return listOf(
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
            BrokerMessage(getRandomLocalDate(2025, 2025), randomNumber()),
        )
    }
}