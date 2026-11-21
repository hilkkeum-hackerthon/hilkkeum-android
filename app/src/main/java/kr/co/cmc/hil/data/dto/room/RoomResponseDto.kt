package kr.co.cmc.hil.data.dto.room

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/rooms/{roomId}` 응답.
 */
@Serializable
data class RoomResponseDto(
    val roomId: Long,
    val memberCount: Int,
    val capacity: Int,
    val currentGameId: Long? = null,
    val members: List<RoomMemberDto>,
)

@Serializable
data class RoomMemberDto(
    val memberId: Long,
    val role: MemberRoleDto,
)

/**
 * 서버가 처음 보는 값을 보내면 역직렬화가 실패해 `safeApiCall`이 SERIALIZATION 에러로 돌려준다.
 */
@Serializable
enum class MemberRoleDto {
    CREATOR,
    JOINER,
}
