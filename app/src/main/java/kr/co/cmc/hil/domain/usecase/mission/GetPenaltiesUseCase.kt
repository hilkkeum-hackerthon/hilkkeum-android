package kr.co.cmc.hil.domain.usecase.mission

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.mission.Penalty
import kr.co.cmc.hil.domain.repository.MissionRepository

/**
 * 상대에게 줄 수 있는 벌칙 목록을 조회한다.
 */
class GetPenaltiesUseCase(
    private val missionRepository: MissionRepository,
) {
    suspend operator fun invoke(): AppResult<List<Penalty>, AppError.Network> =
        missionRepository.getPenalties()
}
