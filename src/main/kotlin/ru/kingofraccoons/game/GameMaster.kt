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
    private var actualHelpCard = mutableMapOf<HelpCard, Int>()
    private var endedHelpCard = mutableListOf<HelpCard>()
    var helpCardMessage = mutableListOf("")
    var statusMessage = mutableMapOf<Pair<String, Int>, String>()
    var tempStatus: Status? = null
    var allOrNothingValue = -1
    var perfectionismValue = 0
    val debts = mutableListOf<String>()
    var notes = listOf<String>()

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
        actionCard?.let { it.hp -= value }
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
        }
    }

    fun getCanUltra() = actionCard?.let { it.costUltra <= it.countSkill } == true

    fun executeAction() {
        when (action) {
            State.hp -> {
                if (increase == -1 && actionCard?.statuses?.keys?.find { it.name == Status.Barrier.name } != null) {
                    actionCard?.statuses?.remove(Status.Barrier)
                    return
                }

                actionCard?.hp = (actionCard?.hp ?: 0) + firstQuantity * increase
            }

            State.shield -> actionCard?.shield = (actionCard?.shield ?: 0) + firstQuantity * increase

            State.power -> power += firstQuantity * increase

            State.skill -> {
                power -= firstQuantity
                actionCard?.countSkill = actionCard?.countSkill?.plus(1) ?: 0
            }

            State.ultra -> {
                power -= firstQuantity
                actionCard?.countSkill = actionCard?.let { it.countSkill.minus(it.costUltra) } ?: 0
            }

            State.status -> {}
            // help card
            else -> {}
        }

        clearQuantities()
    }

    fun executeHelpCard() {
        endedHelpCard.forEach {
            it.cooldown -= 1
        }
        endedHelpCard.removeIf { it.cooldown == 0 }
        actualHelpCard.forEach {
            when (it.key) {
                HelpCard.YourShow -> {
                    helpCardMessage.add("Смена персонажа быстрое действие")
                }

                HelpCard.MissAndMister -> {
                    helpCardMessage.add("Карты подмоги: активный персонаж в конце раунда получает 1 единицу навыка (если навыков максимальное количество, то персонаж с наименьшим количеством единиц навыков)")
                    if (actionCard?.let { it.countSkill < it.costUltra } == true)
                        actionCard?.countSkill = (actionCard?.countSkill ?: 0) + 1
                    else
                        cards.minBy { it.countSkill }.countSkill++
                }

                HelpCard.StudentHighFlight -> {
                    val randomAction = listOf(State.hp, State.shield, State.power).random()
                    helpCardMessage.add("Карты подмоги: СВП - прибавил ${getMessageForIncreaseOrDecreaseButton(randomAction)}")
                    when (randomAction) {
                        State.hp -> cards.random().hp += 2
                        State.power -> power += 2
                        else -> cards.random().shield += 2
                    }
                }

                HelpCard.Mansarda -> {
                    helpCardMessage.add("Карты подмоги: В начале раунда прибавляет оставшиеся в прошлом раунде единицы энергии (до 3)")
                    power += min(3, power)
                }
            }
            actualHelpCard[it.key]?.minus(1)?.let { value -> actualHelpCard[it.key] = value }
        }

        endedHelpCard.addAll(actualHelpCard.keys.filter { it.countMoves == 0 && it.cooldown != 0 })
        actualHelpCard.filter { it.value == 0 }.forEach { actualHelpCard.remove(it.key) }
    }

    fun clearQuantities() {
        firstQuantity = 0
        secondQuantity = 0
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
        tempStatus = null
        statusMessage.clear()
        helpCardMessage.clear()
        endedHelpCard.clear()
        actualHelpCard.clear()
    }

    fun getAllHelpCards() =
        HelpCard.entries.toTypedArray().filter {
            it.name !in endedHelpCard.map { it.name } &&
                    it.name !in actualHelpCard.keys.map { it.name } &&
                    it.cost <= power
        }.chunked(2)

    fun getReplyButtonsInfo() =
        (if (action == State.skill)
            cards.filter { it.canUseSkill }
        else cards).filter { it.hp > 0 && it.canUseSkill }.map { "Персонаж ${it.index}" }

    fun getReplyButtonsInfoWithoutAction() = cards.filter { it != actionCard }.map { "Персонаж ${it.index}" }

    fun printInfo() = cards.map {
        message {
            bold { "Персонаж ${it.index}" } - ": ${it.hp} жизней${Emoji.RedHeart}\n" +
                    "Количество навыков: ${it.countSkill}\n" + "Щит${Emoji.Shield}: ${it.shield}\n" +
                    "Статусы: " + (if (it.statuses.isNotEmpty()) "[\n${it.statuses.keys.joinToString("\n") { "  " + it.title + " - " +  (if (it.inTeam && it == Status.Tox) "каждый ход прибавляется 1 очко навыка" else it.description) }}\n]" else "[]")
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
        cards.forEach { it.executeStatus() }
        tempStatus = null
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
        debts.add(allStudentDebts.random())
    }

    fun setNotes(isIdeal: Boolean = false) {
        notes = if (isIdeal) allNotes.toList() else List(allNotes.size) { allNotes.random() }
    }

    fun getDistinctNotesCount() =
        notes.associateWith { notes.count { note -> note == it } }.toList().maxBy { it.second }.second

    fun isEndDebts() = debts.associateWith { debts.count { debt -> debt == it } }.toList().maxOf { it.second } == 4

    fun getDistinctDebts() =
        debts
            .associateWith { debts.count { debt -> debt == it } }.asSequence().toList()
            .sortedByDescending { it.value }.map { pair -> List(pair.value) { pair.key } }.flatten().take(4)
            .toList()

    fun randomActionOnActionCard(index: Int): Int {
        val randomNumber = (0..3).random()
        when (randomNumber) {
            0 -> actionCard?.let { it.hp += 2 }
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