package ru.kingofraccoons.navigation

object State {
    const val newRound = "Новый раунд"
    const val increase = "increase_"
    const val decrease = "decrease_"

    const val start = "/start"
    const val startRound = "Начать раунд"
    const val startGame = "Начать игру"
    const val inputHealthPoints = "inputHealthPoints"
    const val selectPerson = "selectPerson"
    const val hp = "HP"
    const val increaseHP = increase + hp
    const val decreaseHP = decrease + hp

    const val power = "Power"
    const val increasePower = increase + power
    const val decreasePower = decrease + power

    const val shield = "Shield"
    const val increaseShield = increase + shield
    const val decreaseShield = decrease + shield

    const val skill = "Skill"
    const val ultra = "Ultra"
    const val status = "Status"
    const val helpCard = "Help card"
}