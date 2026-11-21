package kr.co.cmc.hil.domain.model.room

/**
 * 방 생성 결과. 생성자는 방장(CREATOR)이다.
 */
data class CreatedRoom(
    val roomId: RoomId,
    val inviteCode: String,
    val memberId: MemberId,
    val memberToken: String,
)
