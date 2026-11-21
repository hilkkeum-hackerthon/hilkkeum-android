package kr.co.cmc.hil.domain.model

class RoomSession(
    val roomId: Long,
    val memberToken: String,
    val inviteCode: String,
    val isCreator: Boolean,
)
