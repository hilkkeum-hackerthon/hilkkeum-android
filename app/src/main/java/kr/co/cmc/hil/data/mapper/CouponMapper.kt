package kr.co.cmc.hil.data.mapper

import kr.co.cmc.hil.data.dto.coupon.CouponDto
import kr.co.cmc.hil.data.dto.coupon.CouponStatusDto
import kr.co.cmc.hil.data.dto.coupon.CouponUseRequestResponseDto
import kr.co.cmc.hil.data.dto.coupon.CouponsResponseDto
import kr.co.cmc.hil.data.dto.coupon.ExchangeCouponResponseDto
import kr.co.cmc.hil.data.dto.coupon.RespondCouponUseResponseDto
import kr.co.cmc.hil.domain.model.coupon.Coupon
import kr.co.cmc.hil.domain.model.coupon.CouponId
import kr.co.cmc.hil.domain.model.coupon.CouponStatus
import kr.co.cmc.hil.domain.model.coupon.CouponUseRequest
import kr.co.cmc.hil.domain.model.coupon.CouponUseResponse
import kr.co.cmc.hil.domain.model.coupon.ExchangedCoupon
import kr.co.cmc.hil.domain.model.room.MemberId

/**
 * 소원권 교환 응답을 도메인 모델 [ExchangedCoupon]으로 변환한다.
 */
fun ExchangeCouponResponseDto.toDomain(): ExchangedCoupon = ExchangedCoupon(
    couponId = CouponId(couponId),
    memberId = MemberId(memberId),
    heartCount = heartCount,
    issuedAt = coupon.issuedAt,
    expiresAt = coupon.expiresAt,
)

/**
 * 소원권 조회 응답에서 보유 중인 [Coupon]을 꺼낸다. 보유한 소원권이 없으면 null을 반환한다.
 */
fun CouponsResponseDto.toDomain(): Coupon? = coupon?.toDomain()

/**
 * 소원권 사용 요청 응답을 도메인 모델 [CouponUseRequest]로 변환한다.
 */
fun CouponUseRequestResponseDto.toDomain(): CouponUseRequest = CouponUseRequest(
    couponId = CouponId(couponId),
    requestedByMemberId = MemberId(requestedByMemberId),
    waitingForPartner = waitingForPartner,
)

/**
 * 소원권 사용 응답 결과를 도메인 모델 [CouponUseResponse]로 변환한다.
 */
fun RespondCouponUseResponseDto.toDomain(): CouponUseResponse = CouponUseResponse(
    couponId = CouponId(couponId),
    approved = approved,
    usedAt = usedAt,
)

/**
 * 소원권 한 장을 도메인 모델 [Coupon]으로 변환한다.
 */
private fun CouponDto.toDomain(): Coupon = Coupon(
    couponId = CouponId(couponId),
    status = status.toDomain(),
    issuedAt = issuedAt,
    expiresAt = expiresAt,
    remainingDays = remainingDays,
)

/**
 * 서버 응답의 [CouponStatusDto]를 도메인 [CouponStatus]로 변환한다.
 */
private fun CouponStatusDto.toDomain(): CouponStatus = when (this) {
    CouponStatusDto.AVAILABLE -> CouponStatus.AVAILABLE
    CouponStatusDto.PENDING -> CouponStatus.PENDING
    CouponStatusDto.USED -> CouponStatus.USED
    CouponStatusDto.EXPIRED -> CouponStatus.EXPIRED
}
