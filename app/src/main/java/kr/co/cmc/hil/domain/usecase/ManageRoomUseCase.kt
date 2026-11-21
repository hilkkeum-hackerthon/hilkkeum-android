package kr.co.cmc.hil.domain.usecase

import javax.inject.Inject
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.RoomSession
import kr.co.cmc.hil.domain.repository.RoomRepository

class ManageRoomUseCase @Inject constructor(private val repository: RoomRepository) {
    suspend fun create() = repository.create()

    suspend fun join(input: String): AppResult<RoomSession> {
        val code = input.trim()
        return if (code.length == 6 && code.all { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' }) {
            repository.join(code)
        } else {
            AppResult.Failure(AppError.ValidationFailed)
        }
    }

    suspend fun status(session: RoomSession) = repository.status(session)
    suspend fun restore() = repository.restore()
    suspend fun save(session: RoomSession) = repository.save(session)
    suspend fun clear() = repository.clear()
}
