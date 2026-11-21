package kr.co.cmc.hil.data.repositoryimpl

import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.cmc.hil.data.api.MissingRoomServerException
import kr.co.cmc.hil.data.api.RoomHttpException
import kr.co.cmc.hil.data.api.RoomRemoteDataSource
import kr.co.cmc.hil.data.local.RoomSessionDataSource
import kr.co.cmc.hil.data.mapper.toDomain
import kr.co.cmc.hil.domain.model.RoomError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.RoomSession
import kr.co.cmc.hil.domain.repository.RoomRepository

internal class RoomRepositoryImpl @Inject constructor(
    private val remote: RoomRemoteDataSource,
    private val local: RoomSessionDataSource,
) : RoomRepository {
    private suspend fun <T> request(block: suspend () -> T): AppResult<T, RoomError> = try {
        AppResult.Success(block())
    } catch (error: CancellationException) {
        throw error
    } catch (error: MissingRoomServerException) {
        AppResult.Error(RoomError.MissingServer)
    } catch (error: RoomHttpException) {
        AppResult.Error(
            when (error.status) {
                400 -> RoomError.ValidationFailed
                401 -> RoomError.Unauthorized
                403 -> RoomError.PermissionDenied
                404 -> RoomError.NotFound
                409 -> RoomError.RoomFull
                in 500..599 -> RoomError.ServerError
                else -> RoomError.InvalidResponse
            },
        )
    } catch (_: IOException) {
        AppResult.Error(RoomError.NetworkError)
    } catch (_: Exception) {
        AppResult.Error(RoomError.InvalidResponse)
    }

    private suspend fun <T> storage(block: () -> T): AppResult<T, RoomError> = withContext(Dispatchers.IO) {
        try {
            AppResult.Success(block())
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            AppResult.Error(RoomError.StorageError)
        }
    }

    override suspend fun create() = request { remote.create().toDomain() }

    override suspend fun join(inviteCode: String) = request { remote.join(inviteCode).toDomain(inviteCode) }

    override suspend fun status(session: RoomSession) =
        request { remote.status(session.roomId, session.memberToken).toDomain(session.roomId) }

    override suspend fun restore() = storage { local.load() }

    override suspend fun save(session: RoomSession) = storage { local.save(session) }

    override suspend fun clear() = storage { local.clear() }
}
