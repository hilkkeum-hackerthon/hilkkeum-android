package kr.co.cmc.hil.data.dto.coupon

import kotlinx.serialization.Serializable
import kr.co.cmc.hil.data.dto.serializer.OffsetDateTimeSerializer
import java.time.OffsetDateTime

/**
 * `POST /api/v1/coupons/{couponId}/respond` 응답. 거절하면 `usedAt`이 null이다.
 */
@Serializable
data class RespondCouponUseResponseDto(
    val couponId: Long,
    val approved: Boolean,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val usedAt: OffsetDateTime?,
)
