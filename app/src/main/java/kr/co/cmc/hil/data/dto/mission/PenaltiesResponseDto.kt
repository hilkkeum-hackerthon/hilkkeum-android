package kr.co.cmc.hil.data.dto.mission

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/penalties` 응답.
 */
@Serializable
data class PenaltiesResponseDto(
    val penalties: List<PenaltyDto>,
)

@Serializable
data class PenaltyDto(
    val id: Long,
    val content: String,
)
