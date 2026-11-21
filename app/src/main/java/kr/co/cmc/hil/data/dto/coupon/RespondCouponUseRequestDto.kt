package kr.co.cmc.hil.data.dto.coupon

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/coupons/{couponId}/respond` 요청.
 */
@Serializable
data class RespondCouponUseRequestDto(
    val approved: Boolean,
)
