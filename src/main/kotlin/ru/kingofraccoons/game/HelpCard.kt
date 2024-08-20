package ru.kingofraccoons.game

enum class HelpCard(
    val description: String,
    val cost: Int,
    val quantity: Int,
    var countMoves: Int,
    val delay: Int = 0
) {
    YourShow("ТШ ГУАП", 2, 0, 1),
    MissAndMister("МиМ ГУАП", 3, 3, 3),
    StudentHighFlight("СВП ГУАП", 2, 2, 1, delay = 2),
    Mansarda("Мансарда", 4, 3, -1)
}