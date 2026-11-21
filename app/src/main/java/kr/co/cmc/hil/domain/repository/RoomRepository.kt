package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.CreatedRoom

interface RoomRepository {

    /**
     * 새 방을 만들고, 성공하면 발급된 memberToken을 로컬에 저장한다.
     */
    suspend fun createRoom(): AppResult<CreatedRoom, AppError.Network>
}
