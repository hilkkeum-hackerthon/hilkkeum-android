package kr.co.cmc.hil.domain.model.stamp

import kr.co.cmc.hil.domain.model.room.MemberId

/**
 * 내가 모은 스탬프(하트) 현황. 스탬프는 [maxHearts]개까지만 쌓이고, 소원권이 없을 때만 교환할 수 있다.
 */
data class Stamps(
    val memberId: MemberId,
    val heartCount: Int,
    val maxHearts: Int,
    val canExchange: Boolean,
    val hasActiveCoupon: Boolean,
)
