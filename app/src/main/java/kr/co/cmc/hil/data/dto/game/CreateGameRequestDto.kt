package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games` 요청.
 */
@Serializable
data class CreateGameRequestDto(
    val roomId: Long,
    val dateType: DateTypeDto,
    val missionCount: Int,
)
