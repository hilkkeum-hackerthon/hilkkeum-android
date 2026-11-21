package kr.co.cmc.hil.domain.usecase.game

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.game.GameResult
import kr.co.cmc.hil.domain.repository.GameRepository

/**
 * 승패가 확정된 게임의 최종 결과를 조회한다.
 */
class GetGameResultUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(gameId: GameId): AppResult<GameResult, AppError.Network> =
        gameRepository.getGameResult(gameId)
}
