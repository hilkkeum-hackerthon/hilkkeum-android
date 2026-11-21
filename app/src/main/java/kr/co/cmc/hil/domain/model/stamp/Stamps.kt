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
) {
    /** 스탬프가 가득 찼다. */
    val isFull: Boolean
        get() = heartCount >= maxHearts

    /**
     * 스탬프가 가득 찼는데 소원권도 보유 중이라 더 이길 때 스탬프를 받을 수 없는 상태다 (FR-047).
     * 소원권을 쓰거나 만료되기 전까지는 교환할 수도 없다.
     */
    val showFullWarning: Boolean
        get() = isFull && hasActiveCoupon
}
