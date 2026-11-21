package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/games/{gameId}/received-guesses` 응답.
 */
@Serializable
data class ReceivedGuessesResponseDto(
    val gameId: Long,
    val guesses: List<ReceivedGuessDto>,
    val partnerSubmissionCompleted: Boolean,
)

@Serializable
data class ReceivedGuessDto(
    val guessId: Long,
    val guessNo: Int,
    val content: String,
)
