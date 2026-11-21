package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games/{gameId}/judgments` 요청.
 */
@Serializable
data class JudgeGuessRequestDto(
    val guessId: Long,
    val isCorrect: Boolean,
)
