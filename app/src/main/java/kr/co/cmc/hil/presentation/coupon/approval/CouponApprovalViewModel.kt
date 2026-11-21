package kr.co.cmc.hil.presentation.coupon.approval

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.CouponId
import kr.co.cmc.hil.domain.usecase.coupon.RespondCouponUseUseCase
import kr.co.cmc.hil.presentation.common.MviViewModel

/**
 * 파트너가 보낸 소원권 사용 요청을 승인하거나 거절한다 (FR-048a).
 * 응답을 보내는 중에는 중복 응답을 무시한다.
 */
@HiltViewModel
internal class CouponApprovalViewModel @Inject constructor(
    private val respondCouponUse: RespondCouponUseUseCase,
) : MviViewModel<CouponApprovalUiState, CouponApprovalAction, CouponApprovalEvent>(CouponApprovalUiState()) {

    private fun start(couponId: Long) {
        if (uiState.value.couponId == couponId) {
            // 같은 요청으로 다시 들어오면(화면 회전 등) 진행 상태를 유지한다.
        } else {
            updateState { CouponApprovalUiState(couponId = couponId) }
        }
    }

    private suspend fun executeRespond(couponId: Long, approved: Boolean) {
        val result = respondCouponUse(CouponId(couponId), approved)

        when (result) {
            is AppResult.Success -> {
                val outcome = if (result.data.approved) CouponApprovalResult.Approved else CouponApprovalResult.Rejected
                updateState { it.copy(isSubmitting = false, hasError = false, result = outcome) }
                emitEvent(CouponApprovalEvent.Responded)
            }
            is AppResult.Error -> if (result.error == AppError.Network.CONFLICT || result.error == AppError.Network.FORBIDDEN) {
                updateState { it.copy(isSubmitting = false, result = CouponApprovalResult.Unavailable) }
                emitEvent(CouponApprovalEvent.Responded)
            } else {
                updateState { it.copy(isSubmitting = false, hasError = true) }
                emitEvent(CouponApprovalEvent.ShowMessage(R.string.coupon_approval_failed))
            }
        }
    }

    private fun respond(approved: Boolean) {
        val state = uiState.value
        val couponId = state.couponId

        if (!state.canRespond || couponId == null) {
            // 응답 중이거나 이미 응답했다.
        } else {
            updateState { it.copy(isSubmitting = true, hasError = false) }
            viewModelScope.launch { executeRespond(couponId, approved) }
        }
    }

    override fun onAction(action: CouponApprovalAction) {
        when (action) {
            is CouponApprovalAction.Start -> start(action.couponId)
            is CouponApprovalAction.Respond -> respond(action.approved)
        }
    }
}
