package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.RoomError
import kr.co.cmc.hil.domain.model.RoomSession
import kr.co.cmc.hil.domain.model.RoomStatus

interface RoomRepository {
    suspend fun create(): AppResult<RoomSession, RoomError>
    suspend fun join(inviteCode: String): AppResult<RoomSession, RoomError>
    suspend fun status(session: RoomSession): AppResult<RoomStatus, RoomError>
    suspend fun restore(): AppResult<RoomSession?, RoomError>
    suspend fun save(session: RoomSession): AppResult<Unit, RoomError>
    suspend fun clear(): AppResult<Unit, RoomError>
}
