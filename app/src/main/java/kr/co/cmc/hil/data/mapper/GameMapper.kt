package kr.co.cmc.hil.data.mapper

import kr.co.cmc.hil.data.dto.game.ActiveGameResponseDto
import kr.co.cmc.hil.data.dto.game.CreateGameResponseDto
import kr.co.cmc.hil.data.dto.game.DateTypeDto
import kr.co.cmc.hil.data.dto.game.GameOutcomeDto
import kr.co.cmc.hil.data.dto.game.GameResponseDto
import kr.co.cmc.hil.data.dto.game.GameResultResponseDto
import kr.co.cmc.hil.data.dto.game.GameStatusDto
import kr.co.cmc.hil.data.dto.game.GuessRequestDto
import kr.co.cmc.hil.data.dto.game.JudgeGuessResponseDto
import kr.co.cmc.hil.data.dto.game.PlayerResultDto
import kr.co.cmc.hil.data.dto.game.ReceivedGuessDto
import kr.co.cmc.hil.data.dto.game.ReceivedGuessesResponseDto
import kr.co.cmc.hil.data.dto.game.ResultPenaltyDto
import kr.co.cmc.hil.data.dto.game.SelectMissionsResponseDto
import kr.co.cmc.hil.data.dto.game.SelectPenaltyResponseDto
import kr.co.cmc.hil.data.dto.game.SubmitGuessesResponseDto
import kr.co.cmc.hil.domain.model.game.CreatedGame
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.game.Game
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.game.GameOutcome
import kr.co.cmc.hil.domain.model.game.GameResult
import kr.co.cmc.hil.domain.model.game.GameStatus
import kr.co.cmc.hil.domain.model.game.GuessId
import kr.co.cmc.hil.domain.model.game.GuessJudgment
import kr.co.cmc.hil.domain.model.game.GuessSubmission
import kr.co.cmc.hil.domain.model.game.MissionSelection
import kr.co.cmc.hil.domain.model.game.PenaltySelection
import kr.co.cmc.hil.domain.model.game.PlayerId
import kr.co.cmc.hil.domain.model.game.PlayerResult
import kr.co.cmc.hil.domain.model.game.ReceivedGuess
import kr.co.cmc.hil.domain.model.game.ReceivedGuesses
import kr.co.cmc.hil.domain.model.game.ResultPenalty
import kr.co.cmc.hil.domain.model.mission.PenaltyId
import kr.co.cmc.hil.domain.model.room.MemberId
import kr.co.cmc.hil.domain.model.room.RoomId

/**
 * 게임 생성 응답을 도메인 모델 [CreatedGame]으로 변환한다.
 */
fun CreateGameResponseDto.toDomain(): CreatedGame = CreatedGame(
    gameId = GameId(gameId),
    roomId = RoomId(roomId),
    dateType = dateType.toDomain(),
    missionCount = missionCount,
    status = status,
)

/**
 * 게임 상태 조회 응답을 도메인 모델 [Game]으로 변환한다.
 */
fun GameResponseDto.toDomain(): Game = Game(
    gameId = GameId(gameId),
    dateType = dateType.toDomain(),
    missionCount = missionCount,
    status = status.toDomain(),
    mySelectionCompleted = mySelectionCompleted,
    partnerSelectionCompleted = partnerSelectionCompleted,
    endRequestPending = endRequestPending,
)

/**
 * 진행 중인 게임 조회 응답에서 [GameId]를 꺼낸다. 진행 중인 게임이 없으면 null을 반환한다.
 */
fun ActiveGameResponseDto.toDomain(): GameId? = gameId?.let(::GameId)

/**
 * 미션 선택 응답을 도메인 모델 [MissionSelection]으로 변환한다.
 */
fun SelectMissionsResponseDto.toDomain(): MissionSelection = MissionSelection(
    gameId = GameId(gameId),
    selectedCount = selectedCount,
    selectionCompleted = selectionCompleted,
)

/**
 * 벌칙 선택 응답을 도메인 모델 [PenaltySelection]으로 변환한다.
 */
fun SelectPenaltyResponseDto.toDomain(): PenaltySelection = PenaltySelection(
    gameId = GameId(gameId),
    penaltyId = PenaltyId(penaltyId),
    selectionCompleted = selectionCompleted,
)

/**
 * 추리 문장 목록을 요청 본문의 [GuessRequestDto] 목록으로 변환한다. `guessNo`는 목록 순서대로 1부터 매긴다.
 */
fun List<String>.toGuessRequestDtos(): List<GuessRequestDto> = mapIndexed { index, content ->
    GuessRequestDto(guessNo = index + 1, content = content)
}

/**
 * 추리 제출 응답을 도메인 모델 [GuessSubmission]으로 변환한다.
 */
fun SubmitGuessesResponseDto.toDomain(): GuessSubmission = GuessSubmission(
    gameId = GameId(gameId),
    submittedCount = submittedCount,
    submissionCompleted = submissionCompleted,
)

/**
 * 받은 추리 조회 응답을 도메인 모델 [ReceivedGuesses]로 변환한다.
 */
fun ReceivedGuessesResponseDto.toDomain(): ReceivedGuesses = ReceivedGuesses(
    gameId = GameId(gameId),
    guesses = guesses.map { it.toDomain() },
    partnerSubmissionCompleted = partnerSubmissionCompleted,
)

/**
 * 추리 정답 확인 응답을 도메인 모델 [GuessJudgment]로 변환한다.
 */
fun JudgeGuessResponseDto.toDomain(): GuessJudgment = GuessJudgment(
    gameId = GameId(gameId),
    guessId = GuessId(guessId),
    isCorrect = isCorrect,
    winnerPlayerId = winnerPlayerId?.let(::PlayerId),
)

/**
 * 최종 결과 조회 응답을 도메인 모델 [GameResult]로 변환한다.
 */
fun GameResultResponseDto.toDomain(): GameResult = GameResult(
    gameId = GameId(gameId),
    winnerMemberId = MemberId(winnerMemberId),
    myResult = myResult.toDomain(),
    partnerResult = partnerResult.toDomain(),
    players = players.map { it.toDomain() },
    penalty = penalty?.toDomain(),
)

/**
 * 도메인 [DateType]을 요청 본문에 넣을 [DateTypeDto]로 변환한다.
 */
fun DateType.toDto(): DateTypeDto = when (this) {
    DateType.ONLINE -> DateTypeDto.ONLINE
    DateType.OFFLINE -> DateTypeDto.OFFLINE
}

/**
 * 서버 응답의 [DateTypeDto]를 도메인 [DateType]으로 변환한다.
 */
private fun DateTypeDto.toDomain(): DateType = when (this) {
    DateTypeDto.ONLINE -> DateType.ONLINE
    DateTypeDto.OFFLINE -> DateType.OFFLINE
}

/**
 * 받은 추리 한 건을 도메인 모델 [ReceivedGuess]로 변환한다.
 */
private fun ReceivedGuessDto.toDomain(): ReceivedGuess = ReceivedGuess(
    guessId = GuessId(guessId),
    guessNo = guessNo,
    content = content,
)

/**
 * 참가자 한 명의 승패를 도메인 모델 [PlayerResult]로 변환한다.
 */
private fun PlayerResultDto.toDomain(): PlayerResult = PlayerResult(
    memberId = MemberId(memberId),
    result = result.toDomain(),
)

/**
 * 승자가 고른 벌칙을 도메인 모델 [ResultPenalty]로 변환한다.
 */
private fun ResultPenaltyDto.toDomain(): ResultPenalty = ResultPenalty(
    id = PenaltyId(id),
    content = content,
    assignedToMemberId = assignedToMemberId?.let(::MemberId),
)

/**
 * 서버 응답의 [GameOutcomeDto]를 도메인 [GameOutcome]으로 변환한다.
 */
private fun GameOutcomeDto.toDomain(): GameOutcome = when (this) {
    GameOutcomeDto.WIN -> GameOutcome.WIN
    GameOutcomeDto.LOSE -> GameOutcome.LOSE
}

/**
 * 서버 응답의 [GameStatusDto]를 도메인 [GameStatus]로 변환한다.
 */
private fun GameStatusDto.toDomain(): GameStatus = when (this) {
    GameStatusDto.SETTING -> GameStatus.SETTING
    GameStatusDto.PLAYING -> GameStatus.PLAYING
    GameStatusDto.GUESSING -> GameStatus.GUESSING
    GameStatusDto.FINISHED -> GameStatus.FINISHED
}
