package kr.co.cmc.hil.domain.model.game

enum class GameStatus {
    /** 미션·벌칙 선택 중. 선택 완료 여부는 [Game.mySelectionCompleted], [Game.partnerSelectionCompleted]로 구분한다. */
    SETTING,

    /** 양쪽 선택 완료, 작전 진행 중 */
    PLAYING,

    /** 게임 종료, 상대 미션 추리 중 */
    GUESSING,

    /** 승패 결정됨 */
    FINISHED,
}
