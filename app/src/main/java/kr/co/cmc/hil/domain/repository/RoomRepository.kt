package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.RoomSession
import kr.co.cmc.hil.domain.model.RoomStatus

interface RoomRepository {
    suspend fun create(): AppResult<RoomSession>
    suspend fun join(inviteCode: String): AppResult<RoomSession>
    suspend fun status(session: RoomSession): AppResult<RoomStatus>
    suspend fun restore(): AppResult<RoomSession?>
    suspend fun save(session: RoomSession): AppResult<Unit>
    suspend fun clear(): AppResult<Unit>
}
