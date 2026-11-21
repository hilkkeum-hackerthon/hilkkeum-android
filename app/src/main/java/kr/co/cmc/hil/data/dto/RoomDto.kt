package kr.co.cmc.hil.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class CreateRoomDto(
    val roomId: Long,
    val inviteCode: String,
    val memberToken: String,
)

@Serializable
internal data class JoinRoomRequest(val inviteCode: String)

@Serializable
internal data class JoinRoomDto(val roomId: Long, val memberToken: String)

@Serializable
internal data class RoomStatusDto(val roomId: Long, val status: String, val memberCount: Int)
