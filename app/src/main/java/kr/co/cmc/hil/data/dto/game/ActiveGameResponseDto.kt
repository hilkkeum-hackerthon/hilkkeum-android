package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/games/room/{roomId}/active` 응답. 진행 중인 게임이 없으면 `gameId`가 null이다.
 */
@Serializable
data class ActiveGameResponseDto(
    val roomId: Long,
    val gameId: Long?,
)
