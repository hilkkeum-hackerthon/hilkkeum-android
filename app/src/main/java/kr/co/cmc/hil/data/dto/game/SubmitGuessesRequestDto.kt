package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games/{gameId}/guesses` 요청.
 */
@Serializable
data class SubmitGuessesRequestDto(
    val guesses: List<GuessRequestDto>,
)

/**
 * `guessNo`는 1부터 연속, 중복 없이 매긴다.
 */
@Serializable
data class GuessRequestDto(
    val guessNo: Int,
    val content: String,
)
