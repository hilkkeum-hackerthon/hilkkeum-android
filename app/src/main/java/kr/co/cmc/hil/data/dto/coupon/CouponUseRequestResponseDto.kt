package kr.co.cmc.hil.data.dto.coupon

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/coupons/{couponId}/use-request` 응답.
 */
@Serializable
data class CouponUseRequestResponseDto(
    val couponId: Long,
    val requestedByMemberId: Long,
    val waitingForPartner: Boolean,
)
