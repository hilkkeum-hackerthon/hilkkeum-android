package kr.co.cmc.hil.domain.usecase.game

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.game.ReceivedGuesses
import kr.co.cmc.hil.domain.repository.GameRepository

/**
 * 상대가 내 미션에 대해 보낸 추리 목록을 조회한다.
 */
class GetReceivedGuessesUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(gameId: GameId): AppResult<ReceivedGuesses, AppError.Network> =
        gameRepository.getReceivedGuesses(gameId)
}
