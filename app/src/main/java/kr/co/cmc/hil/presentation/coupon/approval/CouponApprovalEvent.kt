package kr.co.cmc.hil.presentation.coupon.approval

import androidx.annotation.StringRes

internal sealed interface CouponApprovalEvent {
    /** 응답이 처리돼 받은 요청 목록을 다시 조회해야 한다. */
    data object Responded : CouponApprovalEvent

    data class ShowMessage(@StringRes val message: Int) : CouponApprovalEvent
}
