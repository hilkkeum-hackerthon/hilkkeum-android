package kr.co.cmc.hil.domain.usecase.game

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.Game
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.repository.GameRepository

/**
 * 게임 상태와 두 참가자의 진행 현황을 조회한다.
 */
class GetGameUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(gameId: GameId): AppResult<Game, AppError.Network> =
        gameRepository.getGame(gameId)
}
