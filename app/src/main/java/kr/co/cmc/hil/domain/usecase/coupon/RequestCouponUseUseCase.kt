package kr.co.cmc.hil.domain.usecase.coupon

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.CouponId
import kr.co.cmc.hil.domain.model.coupon.CouponUseRequest
import kr.co.cmc.hil.domain.repository.CouponRepository

/**
 * 상대에게 소원권 사용 승인을 요청한다.
 */
class RequestCouponUseUseCase(
    private val couponRepository: CouponRepository,
) {
    suspend operator fun invoke(couponId: CouponId): AppResult<CouponUseRequest, AppError.Network> =
        couponRepository.requestCouponUse(couponId)
}
