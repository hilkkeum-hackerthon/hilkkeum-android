package kr.co.cmc.hil.domain.model.coupon

import java.time.OffsetDateTime

/**
 * 소원권 사용 요청에 대한 응답 결과. 승인하면 소원권이 소진되어 [usedAt]이 채워지고, 거절하면 null이다.
 */
data class CouponUseResponse(
    val couponId: CouponId,
    val approved: Boolean,
    val usedAt: OffsetDateTime?,
)
