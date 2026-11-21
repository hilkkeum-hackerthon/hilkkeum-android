package kr.co.cmc.hil.domain.repository

import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.Coupon
import kr.co.cmc.hil.domain.model.coupon.CouponId
import kr.co.cmc.hil.domain.model.coupon.CouponUseRequest
import kr.co.cmc.hil.domain.model.coupon.CouponUseResponse
import kr.co.cmc.hil.domain.model.coupon.ExchangedCoupon

interface CouponRepository {

    /**
     * 스탬프 3개를 차감하고 소원권 1장(발급일로부터 1개월 유효)을 받는다.
     * 스탬프가 부족하거나 이미 소원권을 보유 중이면 [AppError.Network.CONFLICT]로 실패한다.
     *
     * @return 성공하면 발급된 소원권과 남은 스탬프 수, 실패하면 [AppError.Network]
     */
    suspend fun exchangeCoupon(): AppResult<ExchangedCoupon, AppError.Network>

    /**
     * 보유 중인 소원권을 조회한다. 사용 가능한 소원권은 최대 1장이며 만료된 소원권은 포함하지 않는다.
     * 사용 요청 후 상대의 승인을 기다리는 중이면 소원권 상태가 [kr.co.cmc.hil.domain.model.coupon.CouponStatus.PENDING]이다.
     *
     * @return 성공하면 보유 중인 소원권(없으면 null), 실패하면 [AppError.Network]
     */
    suspend fun getCoupon(): AppResult<Coupon?, AppError.Network>

    /**
     * 상대에게 소원권 사용 승인을 요청한다. 상대가 승인하기 전까지 소원권은 소진되지 않는다.
     * 본인 소원권이 아니면 [AppError.Network.FORBIDDEN], 만료·사용 완료·이미 요청 중이면 [AppError.Network.CONFLICT]로 실패한다.
     *
     * @param couponId 사용할 소원권
     * @return 성공하면 요청 결과, 실패하면 [AppError.Network]
     */
    suspend fun requestCouponUse(couponId: CouponId): AppResult<CouponUseRequest, AppError.Network>

    /**
     * 상대가 보낸 소원권 사용 요청을 승인하거나 거절한다. 승인하면 소원권이 소진되고, 거절하면 다시 사용할 수 있는 상태로 남는다.
     * 소원권 소유자 본인은 응답할 수 없고([AppError.Network.FORBIDDEN]), 대기 중인 요청이 없으면 [AppError.Network.CONFLICT]로 실패한다.
     *
     * @param couponId 응답할 소원권
     * @param approved 승인 여부
     * @return 성공하면 응답 결과, 실패하면 [AppError.Network]
     */
    suspend fun respondCouponUse(
        couponId: CouponId,
        approved: Boolean,
    ): AppResult<CouponUseResponse, AppError.Network>
}
