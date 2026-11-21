package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games` 응답.
 */
@Serializable
data class CreateGameResponseDto(
    val gameId: Long,
    val roomId: Long,
    val dateType: DateTypeDto,
    val missionCount: Int,
)
