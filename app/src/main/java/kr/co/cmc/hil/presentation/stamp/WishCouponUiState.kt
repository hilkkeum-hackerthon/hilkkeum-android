package kr.co.cmc.hil.presentation.stamp

import kr.co.cmc.hil.domain.model.coupon.Coupon
import kr.co.cmc.hil.domain.model.coupon.CouponStatus
import kr.co.cmc.hil.domain.model.coupon.IncomingCouponRequest
import kr.co.cmc.hil.domain.model.stamp.Stamps

/** 서버 응답을 받기 전 화면 틀을 그릴 때 쓰는 스탬프 칸 수. */
internal const val DefaultMaxHearts = 3

/** 스탬프 길 끝에 있는 소원권 버튼의 표시 상태. */
internal enum class WishCouponButtonState {
    /** 스탬프가 모자라 교환할 수 없다. */
    Locked,

    /** 스탬프를 소원권으로 교환할 수 있다. */
    Exchangeable,

    /** 보유한 소원권을 사용할 수 있다. */
    Usable,

    /** 사용을 요청하고 파트너의 승인을 기다리는 중이다. */
    Pending,
}

internal data class WishCouponUiState(
    val isLoading: Boolean = true,
    val hasLoadError: Boolean = false,
    val stamps: Stamps? = null,
    val coupon: Coupon? = null,
    val isExchanging: Boolean = false,
    val isRequestingUse: Boolean = false,
    val hasUseRequestError: Boolean = false,
    val incomingRequest: IncomingCouponRequest? = null,
) {
    val heartCount: Int
        get() = stamps?.heartCount ?: 0

    val maxHearts: Int
        get() = stamps?.maxHearts ?: DefaultMaxHearts

    /** 사용 가능하거나 승인 대기 중인 소원권. 사용 완료·만료된 소원권은 보유로 보지 않는다. */
    val activeCoupon: Coupon?
        get() = coupon?.takeIf { it.status == CouponStatus.AVAILABLE || it.status == CouponStatus.PENDING }

    val availableCouponCount: Int
        get() = if (activeCoupon != null) 1 else 0

    val showFullWarning: Boolean
        get() = stamps?.showFullWarning == true

    val hasIncomingRequest: Boolean
        get() = incomingRequest != null

    val buttonState: WishCouponButtonState
        get() = when (activeCoupon?.status) {
            CouponStatus.AVAILABLE -> WishCouponButtonState.Usable
            CouponStatus.PENDING -> WishCouponButtonState.Pending
            else -> if (stamps?.canExchange == true) {
                WishCouponButtonState.Exchangeable
            } else {
                WishCouponButtonState.Locked
            }
        }
}
