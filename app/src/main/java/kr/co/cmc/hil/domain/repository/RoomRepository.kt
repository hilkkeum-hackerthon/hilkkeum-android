package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.CreatedRoom
import kr.co.cmc.hil.domain.model.JoinedRoom

interface RoomRepository {

    /**
     * 새 방을 만들고, 성공하면 발급된 memberToken을 로컬에 저장한다.
     */
    suspend fun createRoom(): AppResult<CreatedRoom, AppError.Network>

    /**
     * 초대 코드로 방에 입장하고, 성공하면 발급된 memberToken을 로컬에 저장한다.
     * 없는 코드는 [AppError.Network.NOT_FOUND], 정원 초과는 [AppError.Network.CONFLICT]로 실패한다.
     */
    suspend fun joinRoom(inviteCode: String): AppResult<JoinedRoom, AppError.Network>
}
