package kr.co.cmc.hil.data.dto.room

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/rooms/join` 응답.
 */
@Serializable
data class JoinRoomResponseDto(
    val roomId: Long,
    val memberId: Long,
    val memberToken: String,
)
