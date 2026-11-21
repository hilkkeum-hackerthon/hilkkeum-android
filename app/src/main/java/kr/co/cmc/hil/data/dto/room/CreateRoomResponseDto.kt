package kr.co.cmc.hil.data.dto.room

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/rooms` 응답.
 */
@Serializable
data class CreateRoomResponseDto(
    val roomId: Long,
    val inviteCode: String,
    val memberId: Long,
    val memberToken: String,
)
