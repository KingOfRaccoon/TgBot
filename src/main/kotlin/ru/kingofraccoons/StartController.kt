package ru.kingofraccoons

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.annotations.CommonHandler
import eu.vendeli.tgbot.annotations.InputHandler
import eu.vendeli.tgbot.api.message.SendMessageAction
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.MessageUpdate
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import eu.vendeli.tgbot.utils.builders.InlineKeyboardMarkupBuilder
import org.kodein.emoji.Emoji
import org.kodein.emoji.objects.tool.Shield
import org.kodein.emoji.smileys_emotion.heart.RedHeart
import org.kodein.emoji.travel_places.sky_weather.Lightning
import ru.kingofraccoons.di.Modules.getGameMaster
import ru.kingofraccoons.game.GameMaster
import ru.kingofraccoons.game.Status
import ru.kingofraccoons.game.StatusName
import ru.kingofraccoons.navigation.State

class StartController {
    @CommandHandler([State.start])
    suspend fun startState(user: User, bot: TelegramBot) {
        getGameMaster(user.id).setDefaultValues()
        message { "Привет, ${user.firstName}" }.replyKeyboardMarkup {
            +State.startGame
            options {
                resizeKeyboard = true
            }
        }.send(user, bot)
    }

    @CommandHandler.CallbackQuery([State.endGame])
    suspend fun restartState(user: User, bot: TelegramBot) {
        getGameMaster(user.id).setDefaultValues()
        message { "Привет, ${user.firstName}" }.replyKeyboardMarkup {
            +State.startGame
            options {
                resizeKeyboard = true
            }
        }.send(user, bot)
    }

    @CommonHandler.Text([State.startGame])
    suspend fun startGameState(user: User, bot: TelegramBot) {
        message { "Введите количество " - bold { "HP каждого персонажа" } - " через пробел (пример: 1 4 8)" }
            .replyKeyboardRemove()
            .send(user, bot)

        bot.navigate(user, State.inputHealthPoints)
    }

    @InputHandler([State.inputHealthPoints])
    suspend fun inputHealthPointsState(update: ProcessedUpdate, user: User, bot: TelegramBot) {
        val splitInput = update.text.trim().split(" ").mapNotNull { it.toIntOrNull() }
        if (splitInput.size != 3) {
            message { "Что-то не так пошло с данными :(\nПопробуйте снова" }.send(user, bot)
            bot.navigate(user, State.inputHealthPoints)
            return
        }

        getGameMaster(user.id).setHealthPoints(splitInput)
        message { "Данные были введены успешно!" }.send(user, bot)
        message { "Введите количество " - bold { "навыков для ульты каждого персонажа" } - " через пробел (пример: 4 2 3)" }
            .replyKeyboardRemove()
            .send(user, bot)
        bot.navigate(user, State.inputSkillsPoints)
    }

    @InputHandler([State.inputSkillsPoints])
    suspend fun inputSkillsPointsState(update: ProcessedUpdate, user: User, bot: TelegramBot) {
        val splitInput = update.text.trim().split(" ").mapNotNull { it.toIntOrNull() }
        if (splitInput.size != 3) {
            message { "Что-то не так пошло с данными :(\nПопробуйте снова" }.send(user, bot)
            bot.navigate(user, State.inputSkillsPoints)
            return
        }

        getGameMaster(user.id).setSkillsPoints(splitInput)
        message { "Данные были введены успешно!" }.replyKeyboardMarkup {
            +State.startRound
            options {
                resizeKeyboard = true
            }
        }.send(user, bot)
    }

    @CommonHandler.Text([State.startRound])
    @CommandHandler.CallbackQuery([State.startRound])
    suspend fun startRoundState(user: User, bot: TelegramBot) {
        startRoundMessages(user, bot)
    }

    @CommandHandler.CallbackQuery([State.status])
    suspend fun selectStatusState(user: User, bot: TelegramBot) {
        message { "Выберите статус" }.inlineKeyboardMarkup {
            Status.entries.toList().filter { it.shown }.chunked(2).forEach { statusChunk ->
                statusChunk.forEach {
                    it.title callback it.title
                }
                br()
            }
        }.send(user, bot)
    }

    @CommandHandler.CallbackQuery(
        [
            StatusName.AllOrNothing,
            StatusName.Tox,
            StatusName.Rat,
            StatusName.StudentDebts,
            StatusName.Roulette,
            StatusName.MusicLottery,
            StatusName.Sophistication,
            StatusName.NineLife,
            StatusName.Provocateur,
            StatusName.FashionableVerdict,
            StatusName.ProperNutrition
        ]
    )
    suspend fun onePersonStatusState(value: ProcessedUpdate, user: User, bot: TelegramBot) {
        val status = Status.entries.toTypedArray().find { it.title == value.text }
        if (status != null)
            getGameMaster(user.id).setStatus(status)
        message { "Выберите персонажа" }.replyKeyboardMarkup {
            getGameMaster(user.id).getReplyButtonsInfo().forEach {
                +it
                br()
            }
            options {
                resizeKeyboard = true
            }
        }.send(user, bot)
    }

    @CommandHandler.CallbackQuery([StatusName.Barrier, StatusName.RedHeadGirlfriend, StatusName.Deadline, StatusName.Faith, StatusName.Perfectionism])
    suspend fun allTeamStatusState(value: ProcessedUpdate, user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        val status = Status.entries.toTypedArray().find { it.title == value.text }
        if (status != null) gameMaster.addStatusInAllCards(status)
        message { "Ко всей команде применен статус " - bold { status?.title.orEmpty() } }.send(user, bot)
        message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    @CommandHandler.CallbackQuery([StatusName.NutsWithMilk, StatusName.Vibe])
    suspend fun allFriendsStatusState(value: ProcessedUpdate, user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        val status = Status.entries.toTypedArray().find { it.title == value.text }
        if (status != null) gameMaster.setStatus(status)
        message { "Выберите персонажа, который применяет статус" }.replyKeyboardMarkup {
            getGameMaster(user.id).getReplyButtonsInfo().forEach {
                +it
                br()
            }
            options {
                resizeKeyboard = true
            }
        }.send(user, bot)
    }

    @CommandHandler.CallbackQuery([State.power])
    suspend fun commandCharacteristicState(value: ProcessedUpdate, user: User, bot: TelegramBot) {
        val action = value.text
        getGameMaster(user.id).setAction(action)
        messageForSelectedAction(action, user, bot) {
            inlineKeyboardMarkup {
                "Увеличить ${getMessageForIncreaseOrDecreaseButton(action)}" callback State.increase + action
                "Уменьшить ${getMessageForIncreaseOrDecreaseButton(action)}" callback State.decrease + action
            }
        }
    }

    @CommandHandler.CallbackQuery([State.hp, State.skill, State.ultra, State.shield])
    suspend fun hpState(value: ProcessedUpdate, user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        val action = value.text
        gameMaster.setAction(action)
        messageForSelectedAction(action, user, bot)
        if (gameMaster.getPerfectionism() && action != State.hp)
            message { "Проверка на идеал" }.inlineKeyboardMarkup {
                "Тест на идеал" callback "Тест на идеал"
                "Меню" callback State.startRound
            }.send(user, bot)
        else
            message { "Выберите персонажа" }.replyKeyboardMarkup {
                getGameMaster(user.id).getReplyButtonsInfo().forEach {
                    +it
                    br()
                }
                options {
                    resizeKeyboard = true
                }
            }.send(user, bot)
    }

    @CommandHandler.CallbackQuery(["Тест на идеал"])
    suspend fun testOnIdeal(user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        if (gameMaster.addPerfectionism()) {
            if (gameMaster.perfectionismValue == 0)
                message { "Проверки закончены, статус " - bold { "Перфекционизм" } - " снят" }.send(user, bot)
            else
                message { "Проверка пройдена. Проверка прошла " - bold { gameMaster.perfectionismValue.toString() } - " раз" }.send(
                    user,
                    bot
                )
            message { "Выберите персонажа" }.replyKeyboardMarkup {
                gameMaster.getReplyButtonsInfo().forEach {
                    +it
                    br()
                }
                options {
                    resizeKeyboard = true
                }
            }.send(user, bot)
        } else {
            message { "Проверка на идеал не пройдена. Все персонажи теряют 1HP" }.send(user, bot)
            gameMaster.decreaseHPAllCards()
        }
    }

    @CommandHandler.CallbackQuery([State.helpCard])
    suspend fun selectHelpCardStates(value: ProcessedUpdate, user: User, bot: TelegramBot) {
        val action = value.text
        getGameMaster(user.id).setAction(action)
        messageForSelectedAction(action, user, bot) {
            replyKeyboardRemove().inlineKeyboardMarkup {
                getGameMaster(user.id).getAllHelpCards().forEach {
                    it.forEach {
                        it.description callback it.description
                    }
                    br()
                }
            }
        }
    }

    @CommandHandler.CallbackQuery([State.selectPerson])
    suspend fun selectPersonState(user: User, bot: TelegramBot) {
        message { "Выберите персонажа" }.replyKeyboardMarkup {
            getGameMaster(user.id).getReplyButtonsInfo().forEach {
                +it
                br()
            }
            options {
                resizeKeyboard = true
            }
        }.send(user, bot)
    }

    @CommandHandler.CallbackQuery([State.increasePower, State.decreasePower, State.increaseHP, State.decreaseHP, State.increaseShield, State.decreaseShield])
    suspend fun setQuantityInHpAndOtherState(value: ProcessedUpdate, user: User, bot: TelegramBot) {
        println(value.text)
        getGameMaster(user.id).setIncrease(if (value.text.contains(State.decrease)) -1 else 1)
        message {
            "Укажите количество ${getMessageForQuantity(value.text.split("_").lastOrNull().orEmpty())}"
        }.inlineKeyboardMarkup {
            enterQuantityInlineKeyboardMarkup(
                endNumber = getGameMaster(user.id).getMaxNumberAction(value.text.contains(State.increase))
            )
        }.send(user, bot)
    }

    @CommonHandler.Regex("Персонаж ([0-9])")
    suspend fun setQuantityState(update: MessageUpdate, user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        val index = "Персонаж ([0-9])".toRegex().find(update.text)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        gameMaster.setActionCard(index)
        if (gameMaster.tempStatus == null && !gameMaster.containsMimHelpCard()) {
            when (val action = gameMaster.getAction().also { println("action: $it") }) {
                State.hp, State.power, State.shield -> {
                    message { "Вы выбрали действие ${getMessageItem(action)}" }.replyKeyboardRemove()
                        .inlineKeyboardMarkup {
                            "Увеличить ${getMessageForIncreaseOrDecreaseButton(action)}" callback State.increase + action
                            "Уменьшить ${getMessageForIncreaseOrDecreaseButton(action)}" callback State.decrease + action
                        }.send(user, bot)
                }

                State.skill -> {
                    messageForSelectedAction(action, user, bot)
                    message { "Сколько энергии стоит навык?" }.replyKeyboardRemove().inlineKeyboardMarkup {
                        enterQuantityInlineKeyboardMarkup(0, gameMaster.getMaxNumberAction(false))
                    }.send(user, bot)
                }

                State.ultra -> {
                    messageForSelectedAction(action, user, bot)
                    message { "Сколько энергии стоит ульта?" }.replyKeyboardRemove().inlineKeyboardMarkup {
                        enterQuantityInlineKeyboardMarkup(1, gameMaster.getMaxNumberAction(false)) {
                            "${State.ultra}_$it"
                        }
                    }.send(user, bot)
                }

                // status
                else -> {
                    messageForSelectedAction(action, user, bot)
                    message { "Выберите персонажа" }.replyKeyboardMarkup {
                        gameMaster.getReplyButtonsInfo().forEach {
                            +it
                            br()
                        }
                        options {
                            resizeKeyboard = true
                        }
                    }.send(user, bot)
                }
            }
        } else {
            if (gameMaster.containsMimHelpCard()) {
                gameMaster.executeHelpCard()
                message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
                startRoundMessages(user, bot)

                return
            }
            if (gameMaster.allOrNothingValue != -1) {
                gameMaster.addShieldInActionCard()
                message { "Отнимите противнику ${gameMaster.getEnemyDecreaseValue(gameMaster.allOrNothingValue)}" }
                    .replyKeyboardRemove()
                    .send(user, bot)
            } else {
                if (gameMaster.tempStatus?.actionIsAllTeam == false) { // Nuts or Vibe
                    gameMaster.addStatusInAllTeamCards()
                    gameMaster.tempStatus?.let {
                        message { "Ко всей команде применен статус " - bold { it.title } }.send(user, bot)
                    }
                } else { // often status
                    when (gameMaster.tempStatus?.title) {
                        StatusName.AllOrNothing -> {
                            message { "Выберите количество щита, которые готовы у себя отнять" }.replyKeyboardRemove()
                                .inlineKeyboardMarkup {
                                    enterQuantityInlineKeyboardMarkup(1, gameMaster.getShieldActionCard(5)) {
                                        "${StatusName.AllOrNothing}_$it"
                                    }
                                }.send(user, bot)
                        }

                        StatusName.Tox -> {
                            message { "Выберите, кто накладывает эффект" }.replyKeyboardRemove()
                                .replyKeyboardMarkup {
                                    +"Эффект наложен на вас"
                                    +"Эффект наложили вы"
                                }.send(user, bot)
                        }

                        StatusName.StudentDebts -> {
                            message {
                                if (gameMaster.debts.isNotEmpty())
                                    "Ваши долги:\n" - bold {
                                        gameMaster.getDistinctDebts().joinToString { it }
                                    }
                                else
                                    "У вас " - bold { "пока что" } - " нет долгов"
                            }.replyKeyboardRemove().replyKeyboardMarkup {
                                if (gameMaster.power > 0)
                                    +"Получить долг"
                                +"Ударить"
                            }.send(user, bot)
                        }

                        StatusName.Roulette -> {
                            message {
                                "На персонажа будет применен случайный эффект:\n" +
                                        " - Прибавить 2hp\n" +
                                        " - Прибавить 3 энергии\n" +
                                        " - Прибавить 2 единицы щита\n" +
                                        " - +2 к следующей атаке персонажа"
                            }.replyKeyboardRemove().send(user, bot)

                            message {
                                "Выпало действие: " -
                                        bold { GameMaster.randomActions[gameMaster.randomActionOnActionCard(index)] }
                            }.send(user, bot)
                            message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
                            startRoundMessages(user, bot)
                        }

                        StatusName.MusicLottery -> {
                            message { "Активирован статус " - bold { "Лотерея нот" } }.replyKeyboardMarkup {
                                +"Вытянуть ноты"
                                br()
                                +"Идеальная мелодия"
                            }.send(user, bot)
                        }

                        StatusName.Sophistication -> {
                            gameMaster.addStatusInCard(Status.Sophistication)
                            message { "На персонажа $index наложен статус " - bold { "Изыск" } }.send(user, bot)
                            message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
                            startRoundMessages(user, bot)
                        }

                        StatusName.Provocateur -> {
                            gameMaster.addStatusInCard(Status.Provocateur)
                            message { "На персонажа $index наложен статус " - bold { "Провокатор" } }.send(
                                user,
                                bot
                            )
                            message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
                            startRoundMessages(user, bot)
                        }

                        StatusName.FashionableVerdict -> {
                            gameMaster.addStatusInCard(Status.FashionableVerdict)
                            message { "На персонажа $index наложен статус " - bold { "Модный приговор" } }.send(
                                user,
                                bot
                            )
                            message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
                            startRoundMessages(user, bot)
                        }

                        StatusName.ProperNutrition -> {
                            gameMaster.addStatusInCard(Status.ProperNutrition)
                            message { "На персонажа $index наложен статус " - bold { "Правильное питание" } }.send(
                                user,
                                bot
                            )
                            message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
                            startRoundMessages(user, bot)
                        }

                        StatusName.Rat -> {
                            gameMaster.addStatusInCard(Status.Rat)
                            message { "На персонажа $index наложен статус " - bold { "Крыса" } }.send(user, bot)
                            message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
                            startRoundMessages(user, bot)
                        }

                        else -> {}
                    }

                    return
                }
            }

            message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
            startRoundMessages(user, bot)
        }
    }

    @CommonHandler.Text(["Вытянуть ноты"])
    suspend fun getNotesState(user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        gameMaster.setNotes()
        message { "Ваша мелодия: \n" + gameMaster.notes.joinToString(" ") { it } }.send(user, bot)
        message {
            bold { "${gameMaster.getDistinctNotesCount()} " } - "одинаковых нот\n" -
                    "Противнику надо снести " - bold { "${gameMaster.getDistinctNotesCount()} HP" }
        }.send(user, bot)

        message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    @CommonHandler.Text(["Идеальная мелодия"])
    suspend fun idealMelodyState(user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        gameMaster.setNotes(true)
        message { "Ваша мелодия: \n" + gameMaster.notes.joinToString(" ") { it } }.send(user, bot)
        message { "Противнику надо снести " - bold { "5 HP" } }.send(user, bot)

        message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    @CommonHandler.Text(["Получить долг"])
    suspend fun getDebtState(user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        gameMaster.addDebt()
        message {
            if (gameMaster.debts.isNotEmpty())
                "Собранные долги:\n" - bold {
                    gameMaster.getDistinctDebts().joinToString { it }
                }
            else
                "У вас " - bold { "пока что" } - " нет долгов"
        }.replyKeyboardRemove().send(user, bot)
        if (gameMaster.isEndDebts()) {
            gameMaster.decreaseHP()
            gameMaster.debts.clear()
            message { "Собрано 4 долга, выбранному персонажу снесено 4 HP" }.send(user, bot)
        } else {
            message {
                "Ваши долги:\n" - bold {
                    gameMaster.getDistinctDebts().joinToString { it }
                }
            }.replyKeyboardMarkup {
                if (gameMaster.power > 0)
                    +"Получить долг"
                +"Ударить"
            }.send(user, bot)

            return
        }

        message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    @CommonHandler.Text(["Ударить"])
    suspend fun debtDamageState(user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)

        message {
            "Собранное количество долгов: " - bold {
                gameMaster.getCountMostPopularDebts().toString()
            }
        }.replyKeyboardRemove()
            .send(user, bot)
        gameMaster.decreaseHP(gameMaster.getCountMostPopularDebts())

        message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    @CommonHandler.Text(["Эффект наложен на вас"])
    suspend fun toxInState(user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        message {
            "На вашу команду наложен статус " - bold { "Токс" } - "\n" +
                    "Ваша команда каждый раунд теряет по 2 HP в течении 3 раундов"
        }.send(user, bot)
        gameMaster.tempStatus?.inTeam = true
        gameMaster.addStatusInCard()

        message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    @CommonHandler.Text(["Эффект наложили вы"])
    suspend fun toxOutState(user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        message {
            "Ваша команда наложила статус " - bold { "Токс" } - "\n" +
                    "Вы каждый раунд получаете по 1 очку навыков в течении 3 раундов"
        }.send(user, bot)
        gameMaster.tempStatus?.inTeam = false
        gameMaster.addStatusInAllCards()
        message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    @CommandHandler.CallbackQuery(["${StatusName.AllOrNothing}_1", "${StatusName.AllOrNothing}_2", "${StatusName.AllOrNothing}_3", "${StatusName.AllOrNothing}_4", "${StatusName.AllOrNothing}_5"])
    suspend fun allOrNothingStatusState(update: ProcessedUpdate, user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        val value =
            "${StatusName.AllOrNothing}_([1-5])".toRegex().find(update.text)?.groupValues?.get(1)?.toIntOrNull() ?: 1
        gameMaster.updateAllOrNothingValue(value)
        message { "Выберите персонажа" }.replyKeyboardMarkup {
            gameMaster.getReplyButtonsInfoWithoutAction().forEach {
                +it
                br()
            }
            options {
                resizeKeyboard = true
            }
        }.send(user, bot)
    }

    @CommandHandler.CallbackQuery(["МиМ ГУАП"])
    suspend fun mimState(update: ProcessedUpdate, user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        gameMaster.addHelpCard(update.text)
        message { "Разыграна карта подмоги: " - bold { update.text } }.send(user, bot)
        message { "Выберите персонажа" }.replyKeyboardMarkup {
            gameMaster.getReplyButtonsInfoWithoutAction().forEach {
                +it
                br()
            }
            options {
                resizeKeyboard = true
            }
        }.send(user, bot)
    }

    @CommandHandler.CallbackQuery(["СВП ГУАП", "ТШ ГУАП", "Мансарда"])
    suspend fun helpCardState(update: ProcessedUpdate, user: User, bot: TelegramBot) {
        getGameMaster(user.id).addHelpCard(update.text)
        message { "Разыграна карта подмоги: " - bold { update.text } }.send(user, bot)

        getGameMaster(user.id).executeHelpCard()
        message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    private suspend fun messageForSelectedAction(
        action: String,
        user: User,
        bot: TelegramBot,
        inlineKeyboardMarkup: (SendMessageAction.() -> SendMessageAction)? = null
    ) {
        if (inlineKeyboardMarkup != null)
            message { "Вы выбрали действие ${getMessageItem(action)}" }.inlineKeyboardMarkup()
                .send(user, bot)
        else
            message { "Вы выбрали действие ${getMessageItem(action)}" }.send(user, bot)
    }

    @CommandHandler.CallbackQuery(
        [State.ultra + "_1", State.ultra + "_2", State.ultra + "_3",
            State.ultra + "_4", State.ultra + "_5", State.ultra + "_6", State.ultra + "_7"]
    )
    suspend fun enterQuantityUltra(update: ProcessedUpdate, user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        gameMaster.setFirstQuantity(update.text.split("_").lastOrNull()?.toIntOrNull() ?: 1)
        if (gameMaster.getCanUltra()) {
            getGameMaster(user.id).executeAction()
            message { "Выполнена ульта" }.replyKeyboardRemove().send(user, bot)
        } else {
            message { "Не хватает использованных навыков для использования ульты" }.replyKeyboardRemove()
                .send(user, bot)
        }
        message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    @CommandHandler.CallbackQuery(["0", "1", "2", "3", "4", "5", "6", "7", "8", "9"])
    suspend fun processQuantityState(update: ProcessedUpdate, user: User, bot: TelegramBot) {
        (update.text.toIntOrNull() ?: 1).let {
            println("quantity: $it")
            if (getGameMaster(user.id).getAction() == State.ultra)
                getGameMaster(user.id).setSecondQuantity(it)
            else
                getGameMaster(user.id).setFirstQuantity(it)
        }
        getGameMaster(user.id).executeAction()
        message { "Следующий ход" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    @CommandHandler.CallbackQuery([State.newRound])
    suspend fun newRoundState(user: User, bot: TelegramBot) {
        getGameMaster(user.id).startNewRound()
        message { "Новый раунд" }.replyKeyboardRemove().send(user, bot)
        startRoundMessages(user, bot)
    }

    private suspend fun startRoundMessages(user: User, bot: TelegramBot) {
        val gameMaster = getGameMaster(user.id)
        gameMaster.clearQuantities()
        gameMaster.helpCardMessage.let { if (it.isNotEmpty()) message { it.joinToString("\n") { it } }.send(user, bot) }
        message { "Энергия${Emoji.Lightning}: ${gameMaster.power}" }.send(user, bot)
        gameMaster.printInfo().forEachIndexed { i, it ->
            if (i == 0)
                it.replyKeyboardRemove().send(user, bot)
            else
                it.send(user, bot)
        }
        if (!gameMaster.containsActionCardRat())
            message { "Выберите действие" }.inlineKeyboardMarkup {
                "HP${Emoji.RedHeart}" callback State.hp
                "Энергия${Emoji.Lightning}" callback State.power
                "Щит${Emoji.Shield}" callback State.shield
                br()
                "Навык" callback State.skill
                "УЛЬТА!" callback State.ultra
                br()
                "Статус" callback State.status
                "Карта подмоги" callback State.helpCard
                br()
                "Новый раунд" callback State.newRound
                "Закончить игру" callback State.endGame
            }.send(user, bot)
        else
            message { "Выберите действие" }.inlineKeyboardMarkup {
                "Новый раунд" callback State.newRound
                "Закончить игру" callback State.endGame
            }.send(user, bot)
    }

    private fun InlineKeyboardMarkupBuilder.enterQuantityInlineKeyboardMarkup(
        startNumber: Int = 1,
        endNumber: Int = 9,
        buttonsInLine: Int = 3,
        callback: (Int) -> String = { it.toString() }
    ) {
        if (startNumber <= endNumber) {
            val buttons = List(endNumber + 1 - startNumber) { it + startNumber }.chunked(buttonsInLine)
            buttons.forEach {
                it.forEach { button ->
                    button.toString() callback callback(button)
                }
                br()
            }
        }
        "Меню" callback State.startRound
    }
}