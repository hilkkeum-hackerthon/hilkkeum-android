package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games/{gameId}/end` 응답.
 */
@Serializable
data class EndGameResponseDto(
    val gameId: Long,
    val ended: Boolean,
)
