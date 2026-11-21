package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games/{gameId}/missions` 응답.
 */
@Serializable
data class SelectMissionsResponseDto(
    val gameId: Long,
    val selectedCount: Int,
    val selectionCompleted: Boolean,
)
