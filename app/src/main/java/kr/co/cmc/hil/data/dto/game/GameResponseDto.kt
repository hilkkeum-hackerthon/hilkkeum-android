package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/games/{gameId}` 응답.
 */
@Serializable
data class GameResponseDto(
    val gameId: Long,
    val dateType: DateTypeDto,
    val missionCount: Int,
    val status: String,
    val mySelectionCompleted: Boolean,
    val partnerSelectionCompleted: Boolean,
    val endRequestPending: Boolean = false,
)
