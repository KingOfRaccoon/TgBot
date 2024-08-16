package ru.kingofraccoons.game

enum class HelpCard(val description: String, val cost: Int, val quantity: Int, var countMoves: Int) {
    YourShow("ТШ ГУАП", 2, 0, 1),
    MissAndMister("МиМ ГУАП", 3, 3, 3),
    StudentHighFlight("СВП ГУАП", 2, 2, 1),
    Mansarda("Мансарда", 4, 3, -1)
}