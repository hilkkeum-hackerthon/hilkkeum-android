package kr.co.cmc.hil.data.repositoryimpl

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kr.co.cmc.hil.data.dto.game.CreateGameRequestDto
import kr.co.cmc.hil.data.dto.game.CreateGameResponseDto
import kr.co.cmc.hil.data.dto.game.GameResponseDto
import kr.co.cmc.hil.data.mapper.toDomain
import kr.co.cmc.hil.data.mapper.toDto
import kr.co.cmc.hil.data.safeApiCall
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.common.map
import kr.co.cmc.hil.domain.model.game.CreatedGame
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.game.Game
import kr.co.cmc.hil.domain.model.game.GameId
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
}
