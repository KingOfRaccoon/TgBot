package ru.kingofraccoons.game

import ru.kingofraccoons.getMessageForIncreaseOrDecreaseButton
import ru.kingofraccoons.navigation.State
import eu.vendeli.tgbot.api.message.message
import org.kodein.emoji.Emoji
import org.kodein.emoji.objects.tool.Shield
import org.kodein.emoji.smileys_emotion.heart.RedHeart
import kotlin.math.min
import kotlin.random.Random

class GameMaster(private val userId: Long) {
    private val cards: MutableList<Card> = mutableListOf()
    var power: Int = startPower
    private var action: String = ""
    private var actionCard: Card? = null
    private var firstQuantity: Int = 0
    private var secondQuantity: Int = 0
    private var increase = 1
    private var statusMessage = mutableMapOf<Pair<String, Int>, String>()
    private var endedHelpCard = mutableMapOf<HelpCard, Int>()
    private var actualHelpCard = mutableMapOf<HelpCard, Int>()
    private var countFury = 0
    var helpCardMessage = mutableMapOf<HelpCard, String>()
    var tempStatus: Status? = null
    var allOrNothingValue = -1
    var perfectionismValue = 0
    var fashionableVerdictContains = false
    var fashionableVerdictDelay = 0
        set(value) {
            field = if (value == -1)
                Status.FashionableVerdict.delay
            else
                value
        }
    val debts = mutableListOf<String>()
    var notes = listOf<String>()

    fun addFury() {
        if (cards.all { it.statuses.containsKey(Status.Fury) }) {
            countFury++
            if (countFury == 7) {
                cards.forEach { it.shield += 3 }
                countFury = 0
            }
        }
    }

    fun getFuryCount() = if (cards.all { it.statuses.containsKey(Status.Fury) }) "Количество ярости: $countFury" else ""
    fun containsMimHelpCard() = actualHelpCard.contains(HelpCard.MissAndMister)

    fun updateAllOrNothingValue(value: Int) {
        actionCard?.let { it.shield -= value }
        allOrNothingValue = value
    }

    fun getEnemyDecreaseValue(value: Int) = when (value) {
        1 -> 1
        2 -> 1
        3 -> 2
        4 -> 3
        else -> 5
    }

    fun getFriendsIncreaseValue(value: Int) = when (value) {
        1 -> 1
        2 -> 2
        3 -> 2
        4 -> 3
        else -> 4
    }

    fun addShieldInActionCard() {
        actionCard?.let { it.shield += getFriendsIncreaseValue(allOrNothingValue) }
    }

    fun decreaseHP(value: Int = 4) {
        actionCard?.changeHP(-value)
    }

    fun addHelpCard(nameHelpCard: String) {
        HelpCard.entries.find { it.description == nameHelpCard }?.let {
            power -= it.cost
            actualHelpCard.put(it, it.countMoves)
        }
    }

    fun setIncrease(increase: Int) {
        this.increase = increase
    }

    fun setFirstQuantity(quantity: Int) {
        firstQuantity = quantity
    }

    fun setSecondQuantity(quantity: Int) {
        secondQuantity = quantity
    }

    fun setAction(action: String) {
        this.action = action
    }

    fun getAction() = action

    fun setActionCard(index: Int) {
        actionCard = cards.find { it.index == index }
    }

    fun containsActionCardRat() = cards.all { it.statuses.keys.contains(Status.Rat) }

    fun getShieldActionCard(defaultValue: Int) = actionCard?.shield ?: defaultValue

    fun getMaxNumberAction(isIncreasing: Boolean): Int {
        if (isIncreasing)
            return 9

        return when (action) {
            State.hp -> actionCard?.let { min(9, it.hp + it.shield) } ?: 9
            State.shield -> actionCard?.shield ?: startShield
            State.power -> power
            State.skill -> min(power, 7)
            State.ultra -> if (firstQuantity != 0) actionCard?.countSkill ?: 5 else min(power, 7)
            else -> power
        }.coerceAtMost(9)
    }

    fun getCanUltra() = actionCard?.let { it.costUltra <= it.countSkill } == true

    fun executeNineLifeInActionCard() = actionCard?.executeNineLife()

    fun executeAction() {
        when (action) {
            State.hp -> {
                actionCard?.changeHP(firstQuantity * increase)
            }

            State.shield -> actionCard?.shield = (actionCard?.shield ?: 0) + firstQuantity * increase

            State.power -> power += firstQuantity * increase

            State.skill -> {
                addFury()
                power -= firstQuantity
                actionCard?.countSkill = actionCard?.countSkill?.plus(1) ?: 0
                if (actionCard?.statuses?.keys?.contains(Status.Sophistication) == true) {
                    actionCard?.statuses?.remove(Status.Sophistication)
                }

                if (actionCard?.statuses?.keys?.contains(Status.ProperNutrition) == true) {
                    actionCard?.statuses?.remove(Status.ProperNutrition)
                }
            }

            State.ultra -> {
                addFury()
                power -= firstQuantity
                actionCard?.countSkill = actionCard?.let { it.countSkill.minus(it.costUltra) } ?: 0
                if (actionCard?.statuses?.keys?.contains(Status.Sophistication) == true) {
                    actionCard?.statuses?.remove(Status.Sophistication)
                }
            }

            State.status -> {}
            // help card
            else -> {}
        }

        clearQuantities()
    }

    fun executeHelpCard(oldPower: Int = power) {
        println(actualHelpCard)
        actualHelpCard.forEach {
            when (it.key) {
                HelpCard.YourShow -> {
                    helpCardMessage[it.key] = "Смена персонажа быстрое действие"
                }

                HelpCard.MissAndMister -> {
                    helpCardMessage[it.key] =
                        "Карты подмоги: активный персонаж в конце раунда получает 1 единицу навыка (если навыков максимальное количество, то персонаж с наименьшим количеством единиц навыков)"
                    if (actionCard?.let { it.countSkill < it.costUltra } == true)
                        actionCard?.countSkill = (actionCard?.countSkill ?: 0) + 1
                    else
                        cards.minBy { it.countSkill }.countSkill++
                }

                HelpCard.StudentHighFlight -> {
                    val randomAction = listOf(State.hp, State.shield, State.power).random()
                    helpCardMessage[it.key] = "Карты подмоги: СВП - прибавил ${
                        getMessageForIncreaseOrDecreaseButton(
                            randomAction
                        )
                    }"
                    when (randomAction) {
                        State.hp -> cards.random().changeHP(2)
                        State.power -> power += 2
                        else -> cards.random().shield += 2
                    }
                }

                HelpCard.Mansarda -> {
                    helpCardMessage[it.key] =
                        "Карты подмоги: В начале раунда прибавляет оставшиеся в прошлом раунде единицы энергии (до 3)"
                    power += min(3, oldPower)
                }
            }
            actualHelpCard[it.key]?.minus(1)?.let { value -> actualHelpCard[it.key] = value }
        }
        endedHelpCard.forEach { (status, _) ->
            endedHelpCard[status]?.minus(1)?.let { value -> endedHelpCard[status] = value }
        }
        helpCardMessage.filter { it.key !in actualHelpCard.keys }.forEach { helpCardMessage.remove(it.key) }

        actualHelpCard.filter { it.value == 0 }.forEach {
            if (it.key.delay != 0) endedHelpCard[it.key] = it.key.delay
            actualHelpCard.remove(it.key)
        }

        endedHelpCard.filter { it.value == 0 }.forEach {
            endedHelpCard.remove(it.key)
        }
    }

    fun clearQuantities() {
        firstQuantity = 0
        secondQuantity = 0
        allOrNothingValue = -1
        tempStatus = null
    }

    // 3 == size hp list
    fun setHealthPoints(healthPoints: List<Int>) {
        healthPoints.forEachIndexed { index, it ->
            cards.add(Card(it, index + 1, userId))
        }
    }

    fun setSkillsPoints(skillPoints: List<Int>) {
        cards.forEachIndexed { index, it ->
            it.costUltra = skillPoints.getOrNull(index) ?: 0
        }
    }

    fun setDefaultValues() {
        cards.clear()
        power = startPower
        action = ""
        actionCard = null
        firstQuantity = 0
        secondQuantity = 0
        increase = 1
        debts.clear()
        notes = listOf()
        allOrNothingValue = -1
        perfectionismValue = 0
        countFury = 0
        tempStatus = null
        statusMessage.clear()
        helpCardMessage.clear()
        endedHelpCard.clear()
        actualHelpCard.clear()
    }

    fun getAllHelpCards() =
        HelpCard.entries.toTypedArray().filter {
            it.name !in endedHelpCard.keys.map { it.name } &&
                    it.name !in actualHelpCard.keys.map { it.name } &&
                    it.cost <= power
        }.chunked(2)

    fun getReplyButtonsInfo() = cards.filter { it.hp > 0 }.map { "Персонаж ${it.index}" }

    fun getDiedCards() = cards.filter { it.hp <= 0 }.map { "Персонаж ${it.index}" }

    fun getReplyButtonsInfoWithoutAction() = cards.filter { it != actionCard }.map { "Персонаж ${it.index}" }

    fun printInfo() = cards.mapIndexed { index, it ->
        message {
            bold { "Персонаж ${it.index}" } - ": ${it.hp} жизней${Emoji.RedHeart}\n" +
                    "Количество навыков: ${it.countSkill}\n" + "Щит${Emoji.Shield}: ${it.shield}\n" +
                    "Статусы: " + (
                    if (it.statusMessages.isNotEmpty())
                        "[\n${it.statusMessages.joinToString("\n") { it }}\n]"
                    else
                        "[]"
                    )
        }
    }

    fun setStatus(status: Status) {
        tempStatus = status
    }

    fun addStatusInCard(currentStatus: Status? = tempStatus) {
        currentStatus?.let { actionCard?.statuses?.put(it, it.countRounds) }
    }

    fun addStatusInAllCards(status: Status? = tempStatus) {
        status?.let {
            cards.forEach { card ->
                card.statuses[it] = it.countRounds
            }
        }
    }

    fun startNewRound() {
//        executeAction()
        fashionableVerdictDelay--
        cards.forEach { it.executeStatus() }
        tempStatus = null
        allOrNothingValue = -1
        val oldPower = power
        power = startPower
        executeHelpCard(oldPower)
    }

    fun addStatusInAllTeamCards() {
        tempStatus?.let { status ->
            cards.filter { it != actionCard }.forEach {
                it.statuses[status] = status.countRounds
            }
        }
    }

    fun addDebt() {
        power--
        repeat(2) {
            deleteNonPopularDebt()
            debts.add(0, allStudentDebts.random())
        }
    }

    fun setNotes(isIdeal: Boolean = false) {
        notes = if (isIdeal) List(allNotes.size) { allNotes.first() } else List(allNotes.size) { allNotes.random() }
    }

    fun getDistinctNotesCount() =
        notes.associateWith { notes.count { note -> note == it } }.toList().maxBy { it.second }.second

    fun getCountMostPopularDebts() =
        debts.associateWith { debts.count { debt -> debt == it } }.toList().maxOf { it.second }

    fun getMostPopularDebts() =
        debts.associateWith { debts.count { debt -> debt == it } }.toList().maxBy { it.second }.first

    fun getSelectedDebtInList(selectedDebt: String) = debts.count { it == selectedDebt }

    fun deleteNonPopularDebt() {
        if (debts.size > 3)
            debts.indexOfLast { it != getMostPopularDebts() }.let {
                if (it != -1)
                    debts.removeAt(it)
            }
    }

    fun isEndDebts() = getCountMostPopularDebts() == 4

    fun getDistinctDebts() =
        debts.associateWith { debts.count { debt -> debt == it } }.asSequence().toList()
            .sortedByDescending { it.value }.map { pair -> List(pair.value) { pair.key } }.flatten().take(4)
            .toList()

    fun randomActionOnActionCard(index: Int): Int {
        val randomNumber = (0..3).random()
        when (randomNumber) {
            0 -> actionCard?.changeHP(2)
            1 -> power += 3
            2 -> actionCard?.let { it.shield += 2 }
            3 -> statusMessage[StatusName.Roulette to index] =
                "+2 к следующей атаке персонажа ${cards.indexOfFirst { it == actionCard }}"
        }

        return randomNumber
    }

    fun getPerfectionism() = cards.all { it.statuses.contains(Status.Perfectionism) }

    fun addPerfectionism(): Boolean {
        perfectionismValue++
        if (perfectionismValue == 5) {
            perfectionismValue = 0
            cards.forEach { it.statuses.remove(Status.Perfectionism) }
        }

        return Random.nextBoolean() || perfectionismValue == 0
    }

    fun decreaseHPAllCards() {
        cards.forEach { it.changeHP(-1) }
    }

    fun containsDiedCards() = cards.any { it.hp == 0 }

    companion object {
        const val startPower = 8
        const val startShield = 0
        val allStudentDebts = arrayOf("Матан", "Английский язык", "История", "Философия", "Физика")
        val randomActions = arrayOf(
            "Прибавить 2hp", "Прибавить 3 энергии", "Прибавить 2 единицы щита", "+2 к следующей атаке персонажа"
        )
        val allNotes = arrayOf("A", "C", "D", "E", "F")
    }
}