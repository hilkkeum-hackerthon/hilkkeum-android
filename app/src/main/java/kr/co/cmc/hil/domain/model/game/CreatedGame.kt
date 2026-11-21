package kr.co.cmc.hil.domain.model.game

import kr.co.cmc.hil.domain.model.room.RoomId

/**
 * 게임 생성 결과. 방장이 정한 데이트 유형과 미션 개수를 담는다.
 */
data class CreatedGame(
    val gameId: GameId,
    val roomId: RoomId,
    val dateType: DateType,
    val missionCount: Int,
    val status: String,
)
