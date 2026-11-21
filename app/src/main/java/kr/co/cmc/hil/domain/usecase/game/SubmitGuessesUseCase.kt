package kr.co.cmc.hil.domain.usecase.game

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.game.GuessSubmission
import kr.co.cmc.hil.domain.repository.GameRepository

/**
 * 상대의 미션을 추리한 문장을 한 번에 제출한다.
 */
class SubmitGuessesUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(
        gameId: GameId,
        guesses: List<String>,
    ): AppResult<GuessSubmission, AppError.Network> =
        gameRepository.submitGuesses(gameId, guesses)
}
