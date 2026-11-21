package kr.co.cmc.hil.domain.usecase.coupon

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.ExchangedCoupon
import kr.co.cmc.hil.domain.repository.CouponRepository

/**
 * 스탬프 3개를 소원권 1장으로 교환한다.
 */
class ExchangeCouponUseCase(
    private val couponRepository: CouponRepository,
) {
    suspend operator fun invoke(): AppResult<ExchangedCoupon, AppError.Network> =
        couponRepository.exchangeCoupon()
}
