package kr.co.cmc.hil.domain.model.coupon

import kr.co.cmc.hil.domain.model.room.MemberId

/**
 * 소원권 사용 요청 결과. 상대가 응답하기 전까지 [waitingForPartner]가 true다.
 */
data class CouponUseRequest(
    val couponId: CouponId,
    val requestedByMemberId: MemberId,
    val waitingForPartner: Boolean,
)
