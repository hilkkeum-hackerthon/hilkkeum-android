package kr.co.cmc.hil.domain.usecase.room

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.room.JoinedRoom
import kr.co.cmc.hil.domain.repository.RoomRepository

/**
 * 방장이 공유한 초대 코드로 방에 입장한다. 입장한 사람은 참여자(JOINER)가 된다.
 */
class JoinRoomUseCase(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(inviteCode: String): AppResult<JoinedRoom, AppError.Network> =
        roomRepository.joinRoom(inviteCode)
}
