package kr.co.cmc.hil.domain.model.game

/**
 * 상대 미션 추리 제출 결과.
 */
data class GuessSubmission(
    val gameId: GameId,
    val submittedCount: Int,
    val submissionCompleted: Boolean,
)
