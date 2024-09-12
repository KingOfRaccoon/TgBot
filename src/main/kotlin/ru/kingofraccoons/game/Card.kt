package ru.kingofraccoons.game

import ru.kingofraccoons.game.GameMaster.Companion.startShield
import kotlin.math.max
import kotlin.math.min

data class Card(private val _hp: Int, var index: Int, val userId: Long, var shield: Int = startShield) {
    var hp = _hp
        set(value) {
            field = max(0, value)
        }

    var costUltra = 0

    fun changeHP(changeValue: Int) {
        if (statuses.containsKey(Status.Barrier) && changeValue < 0) {
            statuses.remove(Status.Barrier)
            return
        }

        val final = if (changeValue >= 0) {
            changeValue
        } else if (shield + changeValue >= 0) {
            shield += changeValue
            0
        } else {
            shield + changeValue.also { shield = 0 }
        }

        hp += final
    }

    var countSkill = 0
        set(value) {
            field = min(costUltra, value)
        }
    val statuses = mutableMapOf<Status, Int>()
    val statusMessages = mutableSetOf<String>()
    private val endedStatuses = mutableMapOf<Status, Int>()

    override fun toString(): String {
        return "Персонаж $index: $hp жизней\n" +
                "Статусы: [\n${statuses.keys.joinToString { "  " + it.title + " - " + it.description }}\n]"
    }
    fun executeNineLife() {
        if (statuses.keys.contains(Status.NineLife)){
            changeHP((2..6).random())
            statuses.remove(Status.NineLife)
        }
    }

    fun executeStatusRedHeadGirlfriend() {
        statuses[Status.RedHeadGirlfriend]?.let {
            statusMessages.add(
                Status.RedHeadGirlfriend.title + " - " +  Status.RedHeadGirlfriend.description
            )
        }
    }

    fun executeStatus() {
        statusMessages.clear()
        statuses.forEach { (it, _) ->
            when (it) {
                Status.RedHeadGirlfriend -> changeHP(it.quantity)
                Status.NutsWithMilk -> changeHP(it.quantity)
                Status.Deadline -> changeHP(it.quantity)
                Status.Vibe -> shield += it.quantity
                Status.Tox -> if (it.inTeam) changeHP(it.quantity) else countSkill++

                Status.NineLife -> changeHP((2..6).random())
                else -> {}
            }
            statuses[it]?.minus(1)?.let { value -> statuses[it] = value }
            statusMessages.add(
                it.title + " - " +
                        (if (!it.inTeam && it == Status.Tox) "каждый ход прибавляется 1 очко навыка" else it.description)
            )
        }
        endedStatuses.forEach { (status, _) ->
            endedStatuses[status]?.minus(1)?.let { value -> endedStatuses[status] = value }
        }

        statuses.filter { it.value == 0 }.forEach {
            if (it.key.delay != 0) endedStatuses[it.key] = it.key.delay
            statuses.remove(it.key)
        }

        endedStatuses.filter { it.value == 0 }.forEach {
            statuses[it.key] = it.key.countRounds
            endedStatuses.remove(it.key)
        }
    }
}
