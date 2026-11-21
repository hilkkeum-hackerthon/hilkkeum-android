package kr.co.cmc.hil.data.mapper

import kr.co.cmc.hil.data.dto.game.CreateGameResponseDto
import kr.co.cmc.hil.data.dto.game.DateTypeDto
import kr.co.cmc.hil.data.dto.game.GameResponseDto
import kr.co.cmc.hil.domain.model.game.CreatedGame
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.game.Game
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.room.RoomId

/**
 * 게임 생성 응답을 도메인 모델 [CreatedGame]으로 변환한다.
 */
fun CreateGameResponseDto.toDomain(): CreatedGame = CreatedGame(
    gameId = GameId(gameId),
    roomId = RoomId(roomId),
    dateType = dateType.toDomain(),
    missionCount = missionCount,
    status = status,
)

/**
 * 게임 상태 조회 응답을 도메인 모델 [Game]으로 변환한다.
 */
fun GameResponseDto.toDomain(): Game = Game(
    gameId = GameId(gameId),
    dateType = dateType.toDomain(),
    missionCount = missionCount,
    status = status,
    mySelectionCompleted = mySelectionCompleted,
    partnerSelectionCompleted = partnerSelectionCompleted,
    endRequestPending = endRequestPending,
)

/**
 * 도메인 [DateType]을 요청 본문에 넣을 [DateTypeDto]로 변환한다.
 */
fun DateType.toDto(): DateTypeDto = when (this) {
    DateType.ONLINE -> DateTypeDto.ONLINE
    DateType.OFFLINE -> DateTypeDto.OFFLINE
}

/**
 * 서버 응답의 [DateTypeDto]를 도메인 [DateType]으로 변환한다.
 */
private fun DateTypeDto.toDomain(): DateType = when (this) {
    DateTypeDto.ONLINE -> DateType.ONLINE
    DateTypeDto.OFFLINE -> DateType.OFFLINE
}
