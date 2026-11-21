package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.stamp.Stamps

interface StampRepository {

    /**
     * 내가 모은 스탬프 수와 소원권 교환 가능 여부를 조회한다.
     * 스탬프가 가득 찼어도 소원권을 보유 중이면 교환할 수 없다.
     *
     * @return 성공하면 스탬프 현황, 실패하면 [AppError.Network]
     */
    suspend fun getStamps(): AppResult<Stamps, AppError.Network>
}
