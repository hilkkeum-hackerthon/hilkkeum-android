package kr.co.cmc.hil.domain.model.coupon

import kr.co.cmc.hil.domain.model.room.MemberId
import java.time.OffsetDateTime

/**
 * 스탬프를 소원권으로 교환한 결과. [heartCount]는 차감 후 남은 스탬프 수다.
 */
data class ExchangedCoupon(
    val couponId: CouponId,
    val memberId: MemberId,
    val heartCount: Int,
    val issuedAt: OffsetDateTime,
    val expiresAt: OffsetDateTime,
)
