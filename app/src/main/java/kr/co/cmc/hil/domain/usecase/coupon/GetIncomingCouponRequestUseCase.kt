package kr.co.cmc.hil.domain.usecase.coupon

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.IncomingCouponRequest
import kr.co.cmc.hil.domain.repository.CouponRepository

/**
 * 상대가 보낸 소원권 사용 요청을 조회한다. 받은 요청이 없으면 null을 반환한다.
 */
class GetIncomingCouponRequestUseCase(
    private val couponRepository: CouponRepository,
) {
    suspend operator fun invoke(): AppResult<IncomingCouponRequest?, AppError.Network> =
        couponRepository.getIncomingCouponRequest()
}
