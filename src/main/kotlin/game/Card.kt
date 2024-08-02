package com.mycompany.game

import com.mycompany.di.Modules
import com.mycompany.game.GameMaster.Companion.startShield

data class Card(private val _hp: Int, var index: Int, val userId: Long, var shield: Int = startShield) {
    var canUseSkill = true
    var hp = _hp
        set(value) {
            field = if (Modules.getUsedNineLife(userId))
                (2..6).random()
            else
                value
        }
    var countSkill = 0
    val statuses = mutableMapOf<Status, Int>()

    override fun toString(): String {
        return "Персонаж $index: $hp жизней\n" +
                "Статусы: [\n${statuses.keys.joinToString { "  " + it.title + " - " + it.description }}\n]"
    }

    fun executeStatus() {
        statuses.forEach { (it, count) ->
            when(it) {
                Status.RedHeadGirlfriend -> hp += it.quantity
                Status.NutsWithMilk -> hp += it.quantity
                Status.Deadline -> hp += it.quantity
                Status.Vibe -> shield += it.quantity
                Status.Tox -> if (it.inTeam) hp += it.quantity else countSkill++
                Status.FashionableVerdict -> {
                    if (it.delay == 0) {
                        canUseSkill = false
                        it.delay = 2
                    } else {
                        it.delay--
                        canUseSkill = true
                    }
                }
                else -> {}
            }
            statuses[it]?.minus(1)?.let { value -> statuses[it] = value }
        }

        statuses.filter { it.value == 0 }.forEach {
            statuses.remove(it.key)
        }
    }
}
