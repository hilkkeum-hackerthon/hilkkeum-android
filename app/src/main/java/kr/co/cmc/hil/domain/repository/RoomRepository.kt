package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.room.CreatedRoom
import kr.co.cmc.hil.domain.model.room.JoinedRoom
import kr.co.cmc.hil.domain.model.room.Room
import kr.co.cmc.hil.domain.model.room.RoomId
import kr.co.cmc.hil.domain.model.room.RoomSession

interface RoomRepository {
    suspend fun restoreSession(): AppResult<RoomSession?, AppError.Network>
    suspend fun clearSession(): AppResult<Unit, AppError.Network>

    /**
     * 새 방을 만들고, 성공하면 발급된 memberToken을 로컬에 저장한다.
     */
    suspend fun createRoom(): AppResult<CreatedRoom, AppError.Network>

    /**
     * 초대 코드로 방에 입장하고, 성공하면 발급된 memberToken을 로컬에 저장한다.
     * 없는 코드는 [AppError.Network.NOT_FOUND], 정원 초과는 [AppError.Network.CONFLICT]로 실패한다.
     */
    suspend fun joinRoom(inviteCode: String): AppResult<JoinedRoom, AppError.Network>

    /**
     * 방 정보와 상대 입장 여부를 조회한다. 방이 없으면 [AppError.Network.NOT_FOUND]로 실패한다.
     */
    suspend fun getRoom(roomId: RoomId): AppResult<Room, AppError.Network>
}
