package kr.co.cmc.hil.presentation.stamp

internal sealed interface WishCouponAction {
    /** 스탬프와 소원권 현황을 다시 조회한다. 화면에 들어올 때와 결산 직후에 보낸다. */
    data object Refresh : WishCouponAction

    /** 홈·스탬프 화면이 보이는 동안 받은 요청과 승인 대기 상태를 폴링한다. */
    data class Observe(val isVisible: Boolean) : WishCouponAction

    data object Exchange : WishCouponAction

    data object RequestUse : WishCouponAction

    /** 소원권 다이얼로그를 다시 열 때 이전 요청 실패 표시를 지운다. */
    data object ClearUseRequestError : WishCouponAction
}
