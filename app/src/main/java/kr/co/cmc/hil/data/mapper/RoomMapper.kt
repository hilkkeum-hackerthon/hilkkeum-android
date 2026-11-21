package kr.co.cmc.hil.data.mapper

import kr.co.cmc.hil.data.dto.room.CreateRoomResponseDto
import kr.co.cmc.hil.data.dto.room.JoinRoomResponseDto
import kr.co.cmc.hil.domain.model.CreatedRoom
import kr.co.cmc.hil.domain.model.JoinedRoom
import kr.co.cmc.hil.domain.model.MemberId
import kr.co.cmc.hil.domain.model.RoomId

fun CreateRoomResponseDto.toDomain(): CreatedRoom = CreatedRoom(
    roomId = RoomId(roomId),
    inviteCode = inviteCode,
    memberId = MemberId(memberId),
    memberToken = memberToken,
)

fun JoinRoomResponseDto.toDomain(): JoinedRoom = JoinedRoom(
    roomId = RoomId(roomId),
    memberId = MemberId(memberId),
    memberToken = memberToken,
)
