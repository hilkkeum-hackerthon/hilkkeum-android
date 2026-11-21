package kr.co.cmc.hil.data.repositoryimpl

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import java.io.IOException
import kotlinx.coroutines.flow.first
import kr.co.cmc.hil.domain.model.room.RoomSession
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kr.co.cmc.hil.data.safeApiCall
import kr.co.cmc.hil.data.datastore.TokenStorage
import kr.co.cmc.hil.data.dto.room.CreateRoomResponseDto
import kr.co.cmc.hil.data.dto.room.JoinRoomRequestDto
import kr.co.cmc.hil.data.dto.room.JoinRoomResponseDto
import kr.co.cmc.hil.data.dto.room.RoomResponseDto
import kr.co.cmc.hil.data.mapper.toDomain
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.common.map
import kr.co.cmc.hil.domain.model.room.CreatedRoom
import kr.co.cmc.hil.domain.model.room.JoinedRoom
import kr.co.cmc.hil.domain.model.room.Room
import kr.co.cmc.hil.domain.model.room.RoomId
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
            .withSavedSession { it.memberToken to RoomSession(it.roomId, it.inviteCode) }
    }

    override suspend fun joinRoom(inviteCode: String): AppResult<JoinedRoom, AppError.Network> {
        return safeApiCall<JoinRoomResponseDto> {
            httpClient.post("api/v1/rooms/join") {
                setBody(JoinRoomRequestDto(inviteCode = inviteCode))
            }
        }
            .map { it.toDomain() }
            .withSavedSession { it.memberToken to RoomSession(it.roomId, null) }
    }

    override suspend fun getRoom(roomId: RoomId): AppResult<Room, AppError.Network> {
        val token = try {
            tokenStorage.memberToken.first()
        } catch (_: IOException) {
            return AppResult.Error(AppError.Network.UNKNOWN)
        }
        if (token.isNullOrBlank()) return AppResult.Error(AppError.Network.UNAUTHORIZED)
        return safeApiCall<RoomResponseDto> {
            httpClient.get("api/v1/rooms/${roomId.value}") { bearerAuth(token) }
        }.map { it.toDomain() }
    }

    override suspend fun restoreSession(): AppResult<RoomSession?, AppError.Network> =
        storageResult { tokenStorage.readRoomSession() }

    override suspend fun clearSession(): AppResult<Unit, AppError.Network> = storageResult {
        tokenStorage.clearRoomSession()
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
        Unit
    }

    private suspend fun <T> AppResult<T, AppError.Network>.withSavedSession(
        sessionOf: (T) -> Pair<String, RoomSession>,
    ): AppResult<T, AppError.Network> = when (this) {
        is AppResult.Error -> this
        is AppResult.Success -> storageResult {
            val (token, session) = sessionOf(data)
            tokenStorage.saveRoomSession(token, session)
            httpClient.authProvider<BearerAuthProvider>()?.clearToken()
            data
        }
    }

    private suspend fun <T> storageResult(block: suspend () -> T): AppResult<T, AppError.Network> =
        try {
            AppResult.Success(block())
        } catch (_: IOException) {
            AppResult.Error(AppError.Network.UNKNOWN)
        }
}
