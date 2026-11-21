package kr.co.cmc.hil.domain.model.room

/**
 * 초대 코드 입장 결과. 입장한 사람은 참여자(JOINER)다.
 */
data class JoinedRoom(
    val roomId: RoomId,
    val memberId: MemberId,
    val memberToken: String,
)
