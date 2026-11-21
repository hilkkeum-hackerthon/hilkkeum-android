package kr.co.cmc.hil.domain.model.game

/**
 * 내 비밀 미션 선택 결과.
 */
data class MissionSelection(
    val gameId: GameId,
    val selectedCount: Int,
    val selectionCompleted: Boolean,
)
