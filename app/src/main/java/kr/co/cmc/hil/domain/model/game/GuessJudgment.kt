package kr.co.cmc.hil.domain.model.game

/**
 * 추리 정답 확인 결과. 정답이면 추리 작성자가 즉시 승리해 [winnerPlayerId]가 채워진다.
 */
data class GuessJudgment(
    val gameId: GameId,
    val guessId: GuessId,
    val isCorrect: Boolean,
    val winnerPlayerId: PlayerId?,
)
