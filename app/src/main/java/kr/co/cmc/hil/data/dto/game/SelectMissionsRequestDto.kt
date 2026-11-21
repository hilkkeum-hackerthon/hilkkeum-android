package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games/{gameId}/missions` 요청.
 */
@Serializable
data class SelectMissionsRequestDto(
    val missionIds: List<Long>,
)
