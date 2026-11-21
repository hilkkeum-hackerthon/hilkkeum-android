package kr.co.cmc.hil.presentation.stamp

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kr.co.cmc.hil.domain.model.WishStampProgress
import kr.co.cmc.hil.domain.usecase.GetWishStampProgressUseCase
import kr.co.cmc.hil.presentation.common.MviViewModel

private const val UsedWishesKey = "wishCoupon.usedWishes"

@HiltViewModel
internal class WishCouponViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    getProgress: GetWishStampProgressUseCase,
) : MviViewModel<WishStampProgress, WishCouponAction, Nothing>(
    initialState = getProgress(savedStateHandle[UsedWishesKey] ?: 0),
) {
    private fun useWish() {
        val updated = uiState.value.useWish()

        savedStateHandle[UsedWishesKey] = updated.usedWishes
        updateState { updated }
    }

    override fun onAction(action: WishCouponAction) {
        when (action) {
            WishCouponAction.UseWish -> useWish()
        }
    }
}
