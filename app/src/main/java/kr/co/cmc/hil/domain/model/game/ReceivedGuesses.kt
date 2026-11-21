package kr.co.cmc.hil.domain.model.game

/**
 * 상대가 내 미션에 대해 보낸 추리 목록. 상대가 아직 제출하지 않았으면 [partnerSubmissionCompleted]가 false다.
 */
data class ReceivedGuesses(
    val gameId: GameId,
    val guesses: List<ReceivedGuess>,
    val partnerSubmissionCompleted: Boolean,
)

data class ReceivedGuess(
    val guessId: GuessId,
    val guessNo: Int,
    val content: String,
)
