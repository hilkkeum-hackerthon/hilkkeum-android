package kr.co.cmc.hil.domain.usecase.room

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.room.CreatedRoom
import kr.co.cmc.hil.domain.repository.RoomRepository

/**
 * 새 방을 만든다. 생성자는 방장(CREATOR)이 된다.
 */
class CreateRoomUseCase(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(): AppResult<CreatedRoom, AppError.Network> =
        roomRepository.createRoom()
}
