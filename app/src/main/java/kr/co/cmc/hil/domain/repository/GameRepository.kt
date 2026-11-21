package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.CreatedGame
import kr.co.cmc.hil.domain.model.game.DateType
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
}
