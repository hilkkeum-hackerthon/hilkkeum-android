package kr.co.cmc.hil.data.repositoryimpl

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import kr.co.cmc.hil.data.safeApiCall
import kr.co.cmc.hil.data.datastore.TokenStorage
import kr.co.cmc.hil.data.dto.room.CreateRoomResponseDto
import kr.co.cmc.hil.data.mapper.toDomain
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.common.map
import kr.co.cmc.hil.domain.common.onSuccess
import kr.co.cmc.hil.domain.model.CreatedRoom
import kr.co.cmc.hil.domain.repository.RoomRepository
import javax.inject.Inject

class RoomRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
) : RoomRepository {

    override suspend fun createRoom(): AppResult<CreatedRoom, AppError.Network> {
        return safeApiCall<CreateRoomResponseDto> {
            httpClient.post("api/v1/rooms")
        }
            .map { it.toDomain() }
            .onSuccess { tokenStorage.saveMemberToken(it.memberToken) }
    }
}
