package kr.co.cmc.hil.domain.model.game

import kr.co.cmc.hil.domain.model.mission.PenaltyId
import kr.co.cmc.hil.domain.model.room.MemberId

/**
 * 승패가 확정된 게임의 최종 결과. 승자가 고른 벌칙이 없으면 [penalty]는 null이다.
 */
data class GameResult(
    val gameId: GameId,
    val winnerMemberId: MemberId,
    val myResult: GameOutcome,
    val partnerResult: GameOutcome,
    val players: List<PlayerResult>,
    val penalty: ResultPenalty?,
)

data class PlayerResult(
    val memberId: MemberId,
    val result: GameOutcome,
)

/**
 * 승자가 골라 패자에게 적용되는 벌칙.
 */
data class ResultPenalty(
    val id: PenaltyId,
    val content: String,
    val assignedToMemberId: MemberId?,
)
