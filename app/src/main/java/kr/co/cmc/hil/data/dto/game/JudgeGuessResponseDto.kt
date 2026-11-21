package kr.co.cmc.hil.data.dto.game

import kotlinx.serialization.Serializable

/**
 * `POST /api/v1/games/{gameId}/judgments` 응답.
 *
 * 명세에는 정답(true) 응답 예시만 있어, 오답일 때 생략될 수 있는 `winnerPlayerId`는 기본값 null로 둔다.
 */
@Serializable
data class JudgeGuessResponseDto(
    val gameId: Long,
    val guessId: Long,
    val isCorrect: Boolean,
    val winnerPlayerId: Long? = null,
)
