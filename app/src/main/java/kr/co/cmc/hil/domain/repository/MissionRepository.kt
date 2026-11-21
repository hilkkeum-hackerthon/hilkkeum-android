package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.mission.Mission
import kr.co.cmc.hil.domain.model.mission.Penalty

interface MissionRepository {

    /**
     * 데이트 유형에 맞는 활성 미션 목록을 조회한다. 등록된 미션이 없으면 빈 목록을 반환한다.
     *
     * @param dateType 데이트 유형(온라인/오프라인)
     * @return 성공하면 미션 목록, 실패하면 [AppError.Network]
     */
    suspend fun getMissions(dateType: DateType): AppResult<List<Mission>, AppError.Network>

    /**
     * 상대에게 줄 수 있는 활성 벌칙 목록을 조회한다. 등록된 벌칙이 없으면 빈 목록을 반환한다.
     *
     * @return 성공하면 벌칙 목록, 실패하면 [AppError.Network]
     */
    suspend fun getPenalties(): AppResult<List<Penalty>, AppError.Network>
}
