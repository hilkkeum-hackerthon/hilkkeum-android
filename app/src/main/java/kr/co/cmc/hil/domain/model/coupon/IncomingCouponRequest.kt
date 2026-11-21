package kr.co.cmc.hil.domain.model.coupon

import java.time.OffsetDateTime

/**
 * 상대가 나에게 보낸 소원권 사용 요청. 승인하거나 거절할 때 [couponId]가 필요하다.
 * 서버가 요청 시각을 주지 않으면 [requestedAt]은 null이다.
 */
data class IncomingCouponRequest(
    val couponId: CouponId,
    val requestedAt: OffsetDateTime?,
)
