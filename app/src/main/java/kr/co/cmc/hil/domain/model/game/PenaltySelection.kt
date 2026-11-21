package kr.co.cmc.hil.domain.model.game

import kr.co.cmc.hil.domain.model.mission.PenaltyId

/**
 * 상대에게 줄 벌칙 선택 결과.
 */
data class PenaltySelection(
    val gameId: GameId,
    val penaltyId: PenaltyId,
    val selectionCompleted: Boolean,
)
