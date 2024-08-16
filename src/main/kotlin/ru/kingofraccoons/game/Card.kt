package ru.kingofraccoons.game

import ru.kingofraccoons.di.Modules
import ru.kingofraccoons.game.GameMaster.Companion.startShield
import kotlin.math.min

data class Card(private val _hp: Int, var index: Int, val userId: Long, var shield: Int = startShield) {
    var costUltra = 0
    var canUseSkill = true
    var hp = _hp
        set(value) {
            field = min(
                _hp, if (shield > 0 && (value - field) < 0) {
                    if (shield >= (field - value)) {
                        shield -= (field - value)
                        field
                    } else {
                        val temp = shield
                        shield = 0
                        ((field - value) - temp)
                    }
                } else {
                    if (value <= 0 && Modules.getUsedNineLife(userId)) {
                        statuses[Status.NineLife] = Status.NineLife.countRounds
                        (2..6).random()
                    } else
                        value
                }
            )
        }
    var countSkill = 0
        set(value) {
            field = min(costUltra, value)
        }
    val statuses = mutableMapOf<Status, Int>()
    private val endedStatuses = mutableMapOf<Status, Int>()

    override fun toString(): String {
        return "Персонаж $index: $hp жизней\n" +
                "Статусы: [\n${statuses.keys.joinToString { "  " + it.title + " - " + it.description }}\n]"
    }

    fun executeStatus() {
        canUseSkill = true
        statuses.forEach { (it, _) ->
            when (it) {
                Status.RedHeadGirlfriend -> hp += it.quantity
                Status.NutsWithMilk -> hp += it.quantity
                Status.Deadline -> hp += it.quantity
                Status.Vibe -> shield += it.quantity
                Status.Tox -> if (it.inTeam) hp += it.quantity else countSkill++
                Status.FashionableVerdict -> { canUseSkill = false }

                else -> {}
            }
            statuses[it]?.minus(1)?.let { value -> statuses[it] = value }
        }

        endedStatuses.forEach { (status, _) ->
            endedStatuses[status]?.minus(1)?.let { value -> statuses[status] = value }
        }

        endedStatuses.filter { it.value == 0 }.forEach {
            statuses[it.key] = it.key.countRounds
            endedStatuses.remove(it.key)
        }

        statuses.filter { it.value == 0 }.forEach {
            if (it.key.delay != 0) endedStatuses[it.key] = it.key.delay
            statuses.remove(it.key)
        }
    }
}
