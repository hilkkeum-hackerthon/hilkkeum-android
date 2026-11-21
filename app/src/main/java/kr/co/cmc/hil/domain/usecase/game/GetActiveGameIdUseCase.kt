package kr.co.cmc.hil.domain.usecase.game

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.room.RoomId
import kr.co.cmc.hil.domain.repository.GameRepository

/**
 * 방에서 진행 중인 게임의 ID를 조회한다. 진행 중인 게임이 없으면 null을 반환한다.
 */
class GetActiveGameIdUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(roomId: RoomId): AppResult<GameId?, AppError.Network> =
        gameRepository.getActiveGameId(roomId)
}
