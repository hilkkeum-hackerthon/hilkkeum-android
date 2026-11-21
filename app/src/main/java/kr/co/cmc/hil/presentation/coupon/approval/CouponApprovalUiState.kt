package kr.co.cmc.hil.presentation.coupon.approval

/** 받은 소원권 사용 요청에 응답한 결과. */
internal enum class CouponApprovalResult {
    Approved,
    Rejected,

    /** 이미 처리됐거나 응답할 수 없는 요청이다. */
    Unavailable,
}

internal data class CouponApprovalUiState(
    val couponId: Long? = null,
    val isSubmitting: Boolean = false,
    val hasError: Boolean = false,
    val result: CouponApprovalResult? = null,
) {
    val canRespond: Boolean
        get() = couponId != null && result == null && !isSubmitting
}
