package kr.co.cmc.hil.domain.usecase.stamp

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.stamp.Stamps
import kr.co.cmc.hil.domain.repository.StampRepository

/**
 * 내가 모은 스탬프 수와 소원권 교환 가능 여부를 조회한다.
 */
class GetStampsUseCase(
    private val stampRepository: StampRepository,
) {
    suspend operator fun invoke(): AppResult<Stamps, AppError.Network> =
        stampRepository.getStamps()
}
