package com.mycompany.game

enum class HelpCard(val description: String, val cost: Int, val quantity: Int, var countMoves: Int, var cooldown: Int) {
    YourShow("ТШ ГУАП", 2, 0, 1, 0),
    MissAndMister("МиМ ГУАП", 3, 3, 3, 0),
    StudentHighFlight("СВП ГУАП", 2, 2, 1, 2),
    Mansarda("Мансарда", 4, 3, -1, 0),
}