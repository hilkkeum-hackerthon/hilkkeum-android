package kr.co.cmc.hil.domain.usecase.game

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.CreatedGame
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.room.RoomId
import kr.co.cmc.hil.domain.repository.GameRepository

/**
 * 방장이 데이트 유형(온라인/오프라인)과 미션 개수를 정해 새 게임을 만든다.
 */
class CreateGameUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(
        roomId: RoomId,
        dateType: DateType,
        missionCount: Int,
    ): AppResult<CreatedGame, AppError.Network> =
        gameRepository.createGame(roomId, dateType, missionCount)
}
