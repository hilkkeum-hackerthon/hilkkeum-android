package kr.co.cmc.hil.domain.model.game

/**
 * 게임 상태와 두 참가자의 진행 현황. 상대의 미션·벌칙 내용은 포함하지 않는다.
 */
data class Game(
    val gameId: GameId,
    val dateType: DateType,
    val missionCount: Int,
    val status: GameStatus,
    val mySelectionCompleted: Boolean,
    val partnerSelectionCompleted: Boolean,
    val endRequestPending: Boolean,
)
