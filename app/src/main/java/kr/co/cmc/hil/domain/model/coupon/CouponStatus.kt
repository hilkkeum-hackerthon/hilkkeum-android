package kr.co.cmc.hil.domain.model.coupon

enum class CouponStatus {
    /** 사용 가능 */
    AVAILABLE,

    /** 상대의 사용 승인 대기 중 */
    PENDING,

    /** 사용 완료 */
    USED,

    /** 만료 */
    EXPIRED,
}
