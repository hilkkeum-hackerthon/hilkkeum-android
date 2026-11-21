package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games/{gameId}/penalty` 요청.
 */
@Serializable
data class SelectPenaltyRequestDto(
    val penaltyId: Long,
)
