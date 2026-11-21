package kr.co.cmc.hil.data.dto.coupon

import kotlinx.serialization.Serializable
import kr.co.cmc.hil.data.dto.serializer.OffsetDateTimeSerializer
import java.time.OffsetDateTime

/**
 * `GET /api/v1/coupons/incoming` 응답 (G8 제안 형식 `{ couponId, requestedAt } | null`).
 * 받은 요청이 없으면 응답 본문이 `null`이다. 서버 확정 전이라 `requestedAt`은 생략될 수 있다.
 */
@Serializable
data class IncomingCouponResponseDto(
    val couponId: Long,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val requestedAt: OffsetDateTime? = null,
)
