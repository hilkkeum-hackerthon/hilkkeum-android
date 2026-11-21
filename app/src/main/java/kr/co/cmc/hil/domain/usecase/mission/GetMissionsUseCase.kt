package kr.co.cmc.hil.domain.usecase.mission

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.mission.Mission
import kr.co.cmc.hil.domain.repository.MissionRepository

/**
 * 데이트 유형에 맞는 선택 가능한 미션 목록을 조회한다.
 */
class GetMissionsUseCase(
    private val missionRepository: MissionRepository,
) {
    suspend operator fun invoke(dateType: DateType): AppResult<List<Mission>, AppError.Network> =
        missionRepository.getMissions(dateType)
}
