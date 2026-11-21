package kr.co.cmc.hil.data.mapper

import kr.co.cmc.hil.data.dto.CreateRoomDto
import kr.co.cmc.hil.data.dto.JoinRoomDto
import kr.co.cmc.hil.data.dto.RoomStatusDto
import kr.co.cmc.hil.domain.model.RoomSession
import kr.co.cmc.hil.domain.model.RoomStatus

internal fun CreateRoomDto.toDomain() = roomSession(roomId, memberToken, inviteCode, isCreator = true)

internal fun JoinRoomDto.toDomain(inviteCode: String) =
    roomSession(roomId, memberToken, inviteCode, isCreator = false)

internal fun RoomStatusDto.toDomain(expectedRoomId: Long): RoomStatus {
    require(roomId == expectedRoomId && memberCount in 1..2 && status.isNotBlank())
    return RoomStatus(roomId, status, memberCount)
}

private fun roomSession(roomId: Long, token: String, code: String, isCreator: Boolean): RoomSession {
    require(roomId > 0 && token.isNotBlank() && code.length == 6)
    return RoomSession(roomId, token, code, isCreator)
}
