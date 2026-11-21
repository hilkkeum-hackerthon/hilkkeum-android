package kr.co.cmc.hil.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WishStampProgressTest {
    @Test
    fun fewerThanThreeStampsCannotBeUsed() {
        val progress = WishStampProgress(listOf("01", "02"))

        assertEquals(0, progress.availableWishes)
        assertEquals(progress, progress.useWish())
    }

    @Test
    fun usingWishRetainsHistoryAndOpensTheNextStampCycle() {
        val progress = WishStampProgress(listOf("01", "02", "03"))
        val used = progress.useWish()

        assertEquals(1, progress.availableWishes)
        assertEquals(1, used.usedWishes)
        assertEquals(progress.earnedDates, used.datesInCycle(0))
        assertEquals(0, used.stampCount)
        assertEquals(0, used.availableWishes)
        assertEquals(used, used.useWish())
    }

    @Test
    fun nextThreeStampsUnlockTheNextWish() {
        val nextCycle = WishStampProgress(listOf("01", "02", "03", "04", "05", "06"), usedWishes = 1)

        assertEquals(listOf("04", "05", "06"), nextCycle.activeDates)
        assertEquals(1, nextCycle.availableWishes)
        assertEquals(2, nextCycle.useWish().usedWishes)
    }
}
