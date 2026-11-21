package kr.co.cmc.hil.domain.usecase.room

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.room.RoomId
import kr.co.cmc.hil.domain.repository.RoomRepository

/**
 * 상대가 들어오기 전에 방장이 만든 방을 삭제하고 로컬 방 세션을 지운다.
 */
class DeleteRoomUseCase(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(roomId: RoomId): AppResult<Unit, AppError.Network> =
        roomRepository.deleteRoom(roomId)
}
