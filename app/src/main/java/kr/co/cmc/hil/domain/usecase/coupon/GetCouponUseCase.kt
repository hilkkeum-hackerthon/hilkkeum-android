package kr.co.cmc.hil.domain.usecase.coupon

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.Coupon
import kr.co.cmc.hil.domain.repository.CouponRepository

/**
 * 보유 중인 소원권을 조회한다. 없으면 null을 반환한다.
 */
class GetCouponUseCase(
    private val couponRepository: CouponRepository,
) {
    suspend operator fun invoke(): AppResult<Coupon?, AppError.Network> =
        couponRepository.getCoupon()
}
