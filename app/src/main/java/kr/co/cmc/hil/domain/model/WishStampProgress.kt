package kr.co.cmc.hil.domain.model

internal const val StampsPerWish = 3

internal data class WishStampProgress(
    val earnedDates: List<String>,
    val usedWishes: Int = 0,
) {
    val activeDates: List<String> get() = datesInCycle(usedWishes)
    val stampCount: Int get() = activeDates.size
    val availableWishes: Int get() = if (stampCount == StampsPerWish) 1 else 0

    fun datesInCycle(index: Int): List<String> =
        earnedDates.drop(index * StampsPerWish).take(StampsPerWish)

    fun useWish(): WishStampProgress =
        if (availableWishes > 0) copy(usedWishes = usedWishes + 1) else this
}
