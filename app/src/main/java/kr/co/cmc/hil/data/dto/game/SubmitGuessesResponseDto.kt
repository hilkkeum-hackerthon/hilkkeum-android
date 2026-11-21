package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games/{gameId}/guesses` 응답.
 */
@Serializable
data class SubmitGuessesResponseDto(
    val gameId: Long,
    val submittedCount: Int,
    val submissionCompleted: Boolean,
)
