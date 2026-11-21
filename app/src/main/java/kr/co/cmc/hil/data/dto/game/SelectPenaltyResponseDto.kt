package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games/{gameId}/penalty` 응답.
 */
@Serializable
data class SelectPenaltyResponseDto(
    val gameId: Long,
    val penaltyId: Long,
    val selectionCompleted: Boolean,
)
