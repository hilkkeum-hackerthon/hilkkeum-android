package kr.co.cmc.hil.data.dto.coupon

import kotlinx.serialization.Serializable
import kr.co.cmc.hil.data.dto.serializer.OffsetDateTimeSerializer
import java.time.OffsetDateTime

/**
 * `GET /api/v1/coupons` 응답. 보유한 소원권이 없으면 `coupon`이 null이다.
 */
@Serializable
data class CouponsResponseDto(
    val hasActiveCoupon: Boolean,
    val coupon: CouponDto?,
)

@Serializable
data class CouponDto(
    val couponId: Long,
    val status: CouponStatusDto,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val issuedAt: OffsetDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val expiresAt: OffsetDateTime,
    val remainingDays: Int,
)
