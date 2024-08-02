package ru.kingofraccoons

import eu.vendeli.tgbot.TelegramBot

suspend fun main(args: Array<String>) {
    val bot = TelegramBot("7464291925:AAE9yVAOYb_m1WtrS6w1rfWNJdcut5B7n9I")

    bot.handleUpdates()
}