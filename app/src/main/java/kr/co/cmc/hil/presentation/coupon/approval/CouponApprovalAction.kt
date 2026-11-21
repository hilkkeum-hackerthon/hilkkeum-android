package kr.co.cmc.hil.presentation.coupon.approval

internal sealed interface CouponApprovalAction {
    /** 승인 화면에 들어올 때 응답할 소원권을 지정한다. 다른 소원권이면 이전 결과를 지운다. */
    data class Start(val couponId: Long) : CouponApprovalAction

    data class Respond(val approved: Boolean) : CouponApprovalAction
}
