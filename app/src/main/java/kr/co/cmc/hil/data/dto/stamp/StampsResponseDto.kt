package kr.co.cmc.hil.data.dto.stamp

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/stamps` 응답.
 */
@Serializable
data class StampsResponseDto(
    val memberId: Long,
    val heartCount: Int,
    val maxHearts: Int,
    val canExchange: Boolean,
    val hasActiveCoupon: Boolean,
)
