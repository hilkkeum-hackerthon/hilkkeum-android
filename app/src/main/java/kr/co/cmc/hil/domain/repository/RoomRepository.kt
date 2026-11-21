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

    /**
     * 방을 삭제하고 로컬 방 세션(memberToken 포함)을 지운다. 상대가 입장하기 전 방장이 방을 나갈 때 쓴다.
     * 방 삭제 API는 서버 명세에 없는 제안 엔드포인트라서 서버 요청은 최선으로만 시도하고,
     * 서버 결과와 상관없이 로컬 세션을 지운다. 로컬 세션을 지우지 못했을 때만 실패한다.
     *
     * @return 로컬 세션을 지우면 성공, 지우지 못하면 [AppError.Network.UNKNOWN]
     */
    suspend fun deleteRoom(roomId: RoomId): AppResult<Unit, AppError.Network>
}
