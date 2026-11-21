package kr.co.cmc.hil.domain.usecase.coupon

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.CouponId
import kr.co.cmc.hil.domain.model.coupon.CouponUseResponse
import kr.co.cmc.hil.domain.repository.CouponRepository

/**
 * 상대가 보낸 소원권 사용 요청을 승인하거나 거절한다.
 */
class RespondCouponUseUseCase(
    private val couponRepository: CouponRepository,
) {
    suspend operator fun invoke(
        couponId: CouponId,
        approved: Boolean,
    ): AppResult<CouponUseResponse, AppError.Network> =
        couponRepository.respondCouponUse(couponId, approved)
}
