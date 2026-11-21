package kr.co.cmc.hil.domain.model.room

/**
 * 방 정보. 상대 입장 여부를 확인할 때 쓴다.
 */
data class Room(
    val roomId: RoomId,
    val memberCount: Int,
    val capacity: Int,
    val currentGameId: Long? = null,
    val members: List<RoomMember>,
) {
    /** 두 명이 모두 입장했는지 여부 */
    val isPartnerJoined: Boolean
        get() = memberCount == 2
}

data class RoomMember(
    val memberId: MemberId,
    val role: MemberRole,
)
