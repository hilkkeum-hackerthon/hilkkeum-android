package kr.co.cmc.hil.domain.usecase.game

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.game.GuessId
import kr.co.cmc.hil.domain.model.game.GuessJudgment
import kr.co.cmc.hil.domain.repository.GameRepository

/**
 * 상대가 보낸 추리 1건의 정답 여부를 확인한다. 정답이면 추리 작성자가 즉시 승리한다.
 */
class JudgeGuessUseCase(
    private val gameRepository: GameRepository,
) {
    suspend operator fun invoke(
        gameId: GameId,
        guessId: GuessId,
        isCorrect: Boolean,
    ): AppResult<GuessJudgment, AppError.Network> =
        gameRepository.judgeGuess(gameId, guessId, isCorrect)
}
