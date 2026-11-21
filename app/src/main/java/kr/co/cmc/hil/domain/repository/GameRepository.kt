package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.CreatedGame
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.game.Game
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.room.RoomId

interface GameRepository {

    /**
     * 방장이 데이트 유형과 미션 개수를 정해 새 게임을 만든다.
     * 방에 두 명이 입장한 경우에만 생성된다.
     *
     * @param roomId 게임을 만들 방
     * @param dateType 데이트 유형(온라인/오프라인)
     * @param missionCount 각자 고를 미션 개수
     * @return 성공하면 생성된 게임 정보, 실패하면 [AppError.Network]
     */
    suspend fun createGame(
        roomId: RoomId,
        dateType: DateType,
        missionCount: Int,
    ): AppResult<CreatedGame, AppError.Network>

    /**
     * 게임 상태와 두 참가자의 진행 현황을 조회한다. 상대의 미션·벌칙 내용은 포함하지 않는다.
     * 참가자가 아니면 [AppError.Network.FORBIDDEN], 게임이 없으면 [AppError.Network.NOT_FOUND]로 실패한다.
     *
     * @param gameId 조회할 게임
     * @return 성공하면 게임 상태와 진행 현황, 실패하면 [AppError.Network]
     */
    suspend fun getGame(gameId: GameId): AppResult<Game, AppError.Network>
}
