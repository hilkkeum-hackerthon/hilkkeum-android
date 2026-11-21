package kr.co.cmc.hil.data.dto.coupon

import kotlinx.serialization.Serializable
import kr.co.cmc.hil.data.dto.serializer.OffsetDateTimeSerializer
import java.time.OffsetDateTime

/**
 * `POST /api/v1/coupons/exchange` 응답.
 */
@Serializable
data class ExchangeCouponResponseDto(
    val couponId: Long,
    val memberId: Long,
    val heartCount: Int,
    val coupon: ExchangedCouponDto,
)

@Serializable
data class ExchangedCouponDto(
    @Serializable(with = OffsetDateTimeSerializer::class)
    val issuedAt: OffsetDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val expiresAt: OffsetDateTime,
)
