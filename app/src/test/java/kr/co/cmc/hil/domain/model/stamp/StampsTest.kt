package kr.co.cmc.hil.domain.model.stamp

import kr.co.cmc.hil.domain.model.room.MemberId
import org.junit.Assert.assertEquals
import org.junit.Test

class StampsTest {

    private fun stamps(heartCount: Int, hasActiveCoupon: Boolean, canExchange: Boolean = false) = Stamps(
        memberId = MemberId(1),
        heartCount = heartCount,
        maxHearts = 3,
        canExchange = canExchange,
        hasActiveCoupon = hasActiveCoupon,
    )

    @Test
    fun showFullWarningOnlyWhenStampsAreFullAndCouponIsHeld() {
        val cases = listOf(
            Triple(0, false, false),
            Triple(2, false, false),
            Triple(3, false, false),
            Triple(0, true, false),
            Triple(2, true, false),
            Triple(3, true, true),
        )

        cases.forEach { (heartCount, hasActiveCoupon, expected) ->
            assertEquals(
                "heartCount=$heartCount, hasActiveCoupon=$hasActiveCoupon",
                expected,
                stamps(heartCount, hasActiveCoupon).showFullWarning,
            )
        }
    }

    @Test
    fun isFullWhenHeartCountReachesMax() {
        assertEquals(false, stamps(2, false).isFull)
        assertEquals(true, stamps(3, false, canExchange = true).isFull)
    }
}
