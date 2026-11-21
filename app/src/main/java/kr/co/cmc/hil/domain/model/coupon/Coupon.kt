package kr.co.cmc.hil.domain.model.coupon

import java.time.OffsetDateTime

/**
 * 보유 중인 소원권. 발급일로부터 1개월 동안 유효하며 D-day 표시에 [remainingDays]를 쓴다.
 * 사용 요청을 보내고 상대의 승인을 기다리는 중이면 [status]가 [CouponStatus.PENDING]이다.
 */
data class Coupon(
    val couponId: CouponId,
    val status: CouponStatus,
    val issuedAt: OffsetDateTime,
    val expiresAt: OffsetDateTime,
    val remainingDays: Int,
)
