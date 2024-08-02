package com.mycompany

import com.mycompany.navigation.State
import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.types.User

fun getMessageItem(value: String) = when (value) {
    State.hp -> "с HP"
    State.power -> "с энергией"
    State.shield -> "со щитом"
    State.skill -> "с навыком"
    State.ultra -> "с ультой"
    State.status -> "со статусом"

    // help card
    else -> "с картой подмоги"
}

fun getMessageForIncreaseOrDecreaseButton(value: String) = when (value) {
    State.hp -> "HP"
    State.power -> "энергию"
    State.shield -> "щит"
    else -> "HP"
}

fun getMessageForQuantity(value: String) = when (value) {
    State.hp -> "HP"
    State.power -> "энергии"
    State.shield -> "щитов"
    else -> "HP"
}

fun TelegramBot.navigate(user: User, state: String) {
    inputListener[user] = state
}