package kr.co.cmc.hil.data.dto.stamp

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/stamps` 응답.
 */
@Serializable
data class StampsResponseDto(
    val memberId: Long? = null,
    val heartCount: Int? = null,
    val maxHearts: Int? = null,
    val canExchange: Boolean? = null,
    val hasActiveCoupon: Boolean,
    val stampCount: Int? = null,
    val maxStamps: Int? = null,
)
