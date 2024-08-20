package ru.kingofraccoons.game

enum class Status(
    val title: String,
    val description: String,
    val quantity: Int = 0,
    var countRounds: Int = 1,
    var delay: Int = 0,
    val actionIsAllTeam: Boolean = true,
    var inTeam: Boolean = false,
    val shown: Boolean = true
) {
    // 0
    Barrier(StatusName.Barrier, "Защита от первой атаки", countRounds = Int.MAX_VALUE),
    RedHeadGirlfriend(
        StatusName.RedHeadGirlfriend,
        "В конце этого и следующего раунда снимает 2hp всей команде",
        -2,
        2,
    ),
    NutsWithMilk(
        StatusName.NutsWithMilk,
        "В конце этого и последующих 2 раундов ТОЛЬКО СОЮЗНИКИ получают по 4hp",
        4,
        3,
        actionIsAllTeam = false
    ),

    // 3
    Deadline(StatusName.Deadline, "В конце этого и последующих 2 раундов союзники теряют по 1hp", -1, 3),

    Faith(
        StatusName.Faith,
        "Увеличивает урон на 1hp",
        1,
        1
    ),

    Perfectionism(StatusName.Perfectionism, "Пройди тест на идеал 5 раз!"),

    //6
    Vibe(
        StatusName.Vibe,
        "В конце этого и последующего раундов союзники получают по 2 щита",
        2,
        2,
        actionIsAllTeam = false
    ),
    AllOrNothing(
        StatusName.AllOrNothing, "Отнимаешь себе n щит, а потом отнимает x hp противнику и добавляет союзнику y щит", -1
    ),
    Tox(StatusName.Tox, "Снимает по 2hp в конце 3 раундов", -2, 3),

    // 9
    Rat(StatusName.Rat, "Блок на все действия", 0),

    StudentDebts(
        StatusName.StudentDebts,
        "Нужно отнимать энергию",
        1
    ),
    Roulette(StatusName.Roulette, "Получаете рандомный бафф", 2),

    // 12
    MusicLottery(StatusName.MusicLottery, "Используем ноты"),

    Sophistication(
        StatusName.Sophistication,
        "Увеличиваем урон на количество энергии",
        countRounds = Int.MAX_VALUE
    ),
    NineLife(StatusName.NineLife, "А кто сказал, что я умер?"),

    // 15
    Provocateur(StatusName.Provocateur, "Все на меня!", countRounds = 2),


    FashionableVerdict(
        StatusName.FashionableVerdict,
        "Я слишком хорош, чтобы ты использовал свои навыки!",
        delay = 2
    ),

    // 18
    ProperNutrition(
        StatusName.ProperNutrition,
        "Следующий навык персонажа снижен на 50% (сносит лишь половину hp, хиллит половину и тд)"
    ),

    // 19
    MisterAndMiss(
        "МиМ ГУАП",
        "активный персонаж в конце раунда получает 1 единицу навыка " +
                "(если навыков максимальное количество, то персонаж с наименьшим количеством единиц навыков)",
        shown = false
    )
}

object StatusName {
    const val Barrier = "Барьер"
    const val RedHeadGirlfriend = "Рыжеволосая подружка"
    const val NutsWithMilk = "Орешки со сгущенкой"
    const val Deadline = "Дедлайн"
    const val Faith = "Вера"
    const val Perfectionism = "Перфикционизм"
    const val Vibe = "Вайб"
    const val AllOrNothing = "Все или ничего"
    const val Tox = "Токс"
    const val Rat = "Крыса"
    const val StudentDebts = "Долги по учебе"
    const val Roulette = "Рулетка"
    const val MusicLottery = "Лотерея нот"
    const val Sophistication = "Изыск"
    const val NineLife = "Девятая жизнь"
    const val Provocateur = "Провокатор"
    const val FashionableVerdict = "Модный приговор"
    const val ProperNutrition = "Правильное питание"
}