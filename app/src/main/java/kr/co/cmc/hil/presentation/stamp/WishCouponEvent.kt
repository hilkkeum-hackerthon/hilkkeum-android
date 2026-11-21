package kr.co.cmc.hil.presentation.stamp

import androidx.annotation.StringRes

internal sealed interface WishCouponEvent {
    /** 교환이 끝나 새 소원권을 보여준다. */
    data object ExchangeCompleted : WishCouponEvent

    /** 사용 요청을 보내 소원권 다이얼로그를 닫는다. */
    data object UseRequested : WishCouponEvent

    data class ShowMessage(@StringRes val message: Int) : WishCouponEvent
}
