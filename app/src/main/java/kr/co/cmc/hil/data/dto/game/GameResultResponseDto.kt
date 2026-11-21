package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/games/{gameId}/result` 응답.
 */
@Serializable
data class GameResultResponseDto(
    val gameId: Long,
    val winnerMemberId: Long,
    val myResult: GameOutcomeDto,
    val partnerResult: GameOutcomeDto,
    val players: List<PlayerResultDto>,
    val penalty: ResultPenaltyDto?,
)

@Serializable
data class PlayerResultDto(
    val memberId: Long,
    val result: GameOutcomeDto,
)

/**
 * 승자가 고른 벌칙. 명세 예시에 없는 `assignedToMemberId`는 생략될 수 있어 기본값 null로 둔다.
 */
@Serializable
data class ResultPenaltyDto(
    val id: Long,
    val content: String,
    val assignedToMemberId: Long? = null,
)

/**
 * 서버가 처음 보는 값을 보내면 역직렬화가 실패해 `safeApiCall`이 SERIALIZATION 에러로 돌려준다.
 */
@Serializable
enum class GameOutcomeDto {
    WIN,
    LOSE,
}
