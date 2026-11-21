package kr.co.cmc.hil.domain.usecase.game

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.EmptyAppResult
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.repository.GameRepository

/**
 * 진행 중인 게임을 즉시 끝내고 추리 단계로 넘긴다.
 */
class EndGameUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(gameId: GameId): EmptyAppResult<AppError.Network> =
        gameRepository.endGame(gameId)
}
