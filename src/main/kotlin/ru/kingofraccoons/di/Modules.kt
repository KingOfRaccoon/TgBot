package ru.kingofraccoons.di

import ru.kingofraccoons.game.GameMaster

object Modules {
    private var gameMasters : MutableMap<Long, GameMaster> = mutableMapOf()
    private var usedNineLife : MutableMap<Long, Boolean> = mutableMapOf()

    fun getGameMaster(userId: Long) : GameMaster {
        if (!gameMasters.containsKey(userId))
            gameMasters[userId] = GameMaster(userId)

        return gameMasters[userId]!!
    }

    fun getUsedNineLife(userId: Long) : Boolean {
        if (!usedNineLife.containsKey(userId)) {
            usedNineLife[userId] = false
            return true
        }

        return usedNineLife[userId] ?: false
    }
}