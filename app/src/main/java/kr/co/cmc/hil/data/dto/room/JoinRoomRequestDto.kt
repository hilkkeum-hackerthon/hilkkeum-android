package kr.co.cmc.hil.data.dto.room

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/rooms/join` 요청.
 */
@Serializable
data class JoinRoomRequestDto(
    val inviteCode: String,
)
