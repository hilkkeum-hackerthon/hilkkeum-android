package kr.co.cmc.hil.domain.usecase.room

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.Room
import kr.co.cmc.hil.domain.model.RoomId
import kr.co.cmc.hil.domain.repository.RoomRepository

/**
 * 방 정보를 조회한다. 상대 입장 여부는 [Room.isPartnerJoined]로 확인한다.
 */
class GetRoomUseCase(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(roomId: RoomId): AppResult<Room, AppError.Network> =
        roomRepository.getRoom(roomId)
}
