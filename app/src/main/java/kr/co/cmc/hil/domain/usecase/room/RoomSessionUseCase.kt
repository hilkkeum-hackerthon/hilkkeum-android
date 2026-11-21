package kr.co.cmc.hil.domain.usecase.room

import kr.co.cmc.hil.domain.repository.RoomRepository

class RoomSessionUseCase(private val repository: RoomRepository) {
    suspend operator fun invoke() = repository.restoreSession()
    suspend fun clear() = repository.clearSession()
}
