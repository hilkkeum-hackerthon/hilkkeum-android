package kr.co.cmc.hil.data.repositoryimpl

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kr.co.cmc.hil.data.dto.game.ActiveGameResponseDto
import kr.co.cmc.hil.data.dto.game.CreateGameRequestDto
import kr.co.cmc.hil.data.dto.game.CreateGameResponseDto
import kr.co.cmc.hil.data.dto.game.EndGameResponseDto
import kr.co.cmc.hil.data.dto.game.GameResponseDto
import kr.co.cmc.hil.data.dto.game.GameResultResponseDto
import kr.co.cmc.hil.data.dto.game.JudgeGuessRequestDto
import kr.co.cmc.hil.data.dto.game.JudgeGuessResponseDto
import kr.co.cmc.hil.data.dto.game.ReceivedGuessesResponseDto
import kr.co.cmc.hil.data.dto.game.SelectMissionsRequestDto
import kr.co.cmc.hil.data.dto.game.SelectMissionsResponseDto
import kr.co.cmc.hil.data.dto.game.SelectPenaltyRequestDto
import kr.co.cmc.hil.data.dto.game.SelectPenaltyResponseDto
import kr.co.cmc.hil.data.dto.game.SubmitGuessesRequestDto
import kr.co.cmc.hil.data.dto.game.SubmitGuessesResponseDto
import kr.co.cmc.hil.data.mapper.toDomain
import kr.co.cmc.hil.data.mapper.toDto
import kr.co.cmc.hil.data.mapper.toGuessRequestDtos
import kr.co.cmc.hil.data.safeApiCall
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.common.EmptyAppResult
import kr.co.cmc.hil.domain.common.asEmptyResult
import kr.co.cmc.hil.domain.common.map
import kr.co.cmc.hil.domain.model.game.CreatedGame
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.game.Game
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.game.GameResult
import kr.co.cmc.hil.domain.model.game.GuessId
import kr.co.cmc.hil.domain.model.game.GuessJudgment
import kr.co.cmc.hil.domain.model.game.GuessSubmission
import kr.co.cmc.hil.domain.model.game.MissionSelection
import kr.co.cmc.hil.domain.model.game.PenaltySelection
import kr.co.cmc.hil.domain.model.game.ReceivedGuesses
import kr.co.cmc.hil.domain.model.mission.MissionId
import kr.co.cmc.hil.domain.model.mission.PenaltyId
import kr.co.cmc.hil.domain.model.room.RoomId
import kr.co.cmc.hil.domain.repository.GameRepository
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
) : GameRepository {

    /**
     * `POST api/v1/games`를 호출하고 응답을 [CreatedGame]으로 변환한다.
     */
    override suspend fun createGame(
        roomId: RoomId,
        dateType: DateType,
        missionCount: Int,
    ): AppResult<CreatedGame, AppError.Network> {
        return safeApiCall<CreateGameResponseDto> {
            httpClient.post("api/v1/games") {
                setBody(
                    CreateGameRequestDto(
                        roomId = roomId.value,
                        dateType = dateType.toDto(),
                        missionCount = missionCount,
                    ),
                )
            }
        }.map { it.toDomain() }
    }

    /**
     * `GET api/v1/games/{gameId}`를 호출하고 응답을 [Game]으로 변환한다.
     */
    override suspend fun getGame(gameId: GameId): AppResult<Game, AppError.Network> {
        return safeApiCall<GameResponseDto> {
            httpClient.get("api/v1/games/${gameId.value}")
        }.map { it.toDomain() }
    }

    /**
     * `GET api/v1/games/room/{roomId}/active`를 호출하고 응답에서 [GameId]를 꺼낸다.
     */
    override suspend fun getActiveGameId(roomId: RoomId): AppResult<GameId?, AppError.Network> {
        return safeApiCall<ActiveGameResponseDto> {
            httpClient.get("api/v1/games/room/${roomId.value}/active")
        }.map { it.toDomain() }
    }

    /**
     * `POST api/v1/games/{gameId}/missions`를 호출하고 응답을 [MissionSelection]으로 변환한다.
     */
    override suspend fun selectMissions(
        gameId: GameId,
        missionIds: List<MissionId>,
    ): AppResult<MissionSelection, AppError.Network> {
        return safeApiCall<SelectMissionsResponseDto> {
            httpClient.post("api/v1/games/${gameId.value}/missions") {
                setBody(SelectMissionsRequestDto(missionIds = missionIds.map { it.value }))
            }
        }.map { it.toDomain() }
    }

    /**
     * `POST api/v1/games/{gameId}/penalty`를 호출하고 응답을 [PenaltySelection]으로 변환한다.
     */
    override suspend fun selectPenalty(
        gameId: GameId,
        penaltyId: PenaltyId,
    ): AppResult<PenaltySelection, AppError.Network> {
        return safeApiCall<SelectPenaltyResponseDto> {
            httpClient.post("api/v1/games/${gameId.value}/penalty") {
                setBody(SelectPenaltyRequestDto(penaltyId = penaltyId.value))
            }
        }.map { it.toDomain() }
    }

    /**
     * `POST api/v1/games/{gameId}/end`를 호출한다. 응답 본문은 성공 여부 확인에만 쓰고 버린다.
     */
    override suspend fun endGame(gameId: GameId): EmptyAppResult<AppError.Network> {
        return safeApiCall<EndGameResponseDto> {
            httpClient.post("api/v1/games/${gameId.value}/end")
        }.asEmptyResult()
    }

    /**
     * `POST api/v1/games/{gameId}/guesses`를 호출하고 응답을 [GuessSubmission]으로 변환한다.
     * 추리 문장은 목록 순서대로 `guessNo`를 1부터 매겨 보낸다.
     */
    override suspend fun submitGuesses(
        gameId: GameId,
        guesses: List<String>,
    ): AppResult<GuessSubmission, AppError.Network> {
        return safeApiCall<SubmitGuessesResponseDto> {
            httpClient.post("api/v1/games/${gameId.value}/guesses") {
                setBody(SubmitGuessesRequestDto(guesses = guesses.toGuessRequestDtos()))
            }
        }.map { it.toDomain() }
    }

    /**
     * `GET api/v1/games/{gameId}/received-guesses`를 호출하고 응답을 [ReceivedGuesses]로 변환한다.
     */
    override suspend fun getReceivedGuesses(gameId: GameId): AppResult<ReceivedGuesses, AppError.Network> {
        return safeApiCall<ReceivedGuessesResponseDto> {
            httpClient.get("api/v1/games/${gameId.value}/received-guesses")
        }.map { it.toDomain() }
    }

    /**
     * `POST api/v1/games/{gameId}/judgments`를 호출하고 응답을 [GuessJudgment]로 변환한다.
     */
    override suspend fun judgeGuess(
        gameId: GameId,
        guessId: GuessId,
        isCorrect: Boolean,
    ): AppResult<GuessJudgment, AppError.Network> {
        return safeApiCall<JudgeGuessResponseDto> {
            httpClient.post("api/v1/games/${gameId.value}/judgments") {
                setBody(JudgeGuessRequestDto(guessId = guessId.value, isCorrect = isCorrect))
            }
        }.map { it.toDomain() }
    }

    /**
     * `GET api/v1/games/{gameId}/result`를 호출하고 응답을 [GameResult]로 변환한다.
     */
    override suspend fun getGameResult(gameId: GameId): AppResult<GameResult, AppError.Network> {
        return safeApiCall<GameResultResponseDto> {
            httpClient.get("api/v1/games/${gameId.value}/result")
        }.map { it.toDomain() }
    }
}
