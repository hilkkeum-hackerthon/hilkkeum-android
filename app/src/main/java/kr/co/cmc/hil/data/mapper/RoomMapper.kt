package kr.co.cmc.hil.data.mapper

import kr.co.cmc.hil.data.dto.room.CreateRoomResponseDto
import kr.co.cmc.hil.data.dto.room.JoinRoomResponseDto
import kr.co.cmc.hil.data.dto.room.MemberRoleDto
import kr.co.cmc.hil.data.dto.room.RoomMemberDto
import kr.co.cmc.hil.data.dto.room.RoomResponseDto
import kr.co.cmc.hil.domain.model.room.CreatedRoom
import kr.co.cmc.hil.domain.model.room.JoinedRoom
import kr.co.cmc.hil.domain.model.room.MemberId
import kr.co.cmc.hil.domain.model.room.MemberRole
import kr.co.cmc.hil.domain.model.room.Room
import kr.co.cmc.hil.domain.model.room.RoomId
import kr.co.cmc.hil.domain.model.room.RoomMember

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

fun RoomResponseDto.toDomain(): Room = Room(
    roomId = RoomId(roomId),
    memberCount = memberCount,
    capacity = capacity,
    currentGameId = currentGameId,
    members = members.map { it.toDomain() },
)

private fun RoomMemberDto.toDomain(): RoomMember = RoomMember(
    memberId = MemberId(memberId),
    role = role.toDomain(),
)

private fun MemberRoleDto.toDomain(): MemberRole = when (this) {
    MemberRoleDto.CREATOR -> MemberRole.CREATOR
    MemberRoleDto.JOINER -> MemberRole.JOINER
}
