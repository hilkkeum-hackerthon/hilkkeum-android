package kr.co.cmc.hil.presentation.stamp

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.Coupon
import kr.co.cmc.hil.domain.model.coupon.CouponStatus
import kr.co.cmc.hil.domain.usecase.coupon.ExchangeCouponUseCase
import kr.co.cmc.hil.domain.usecase.coupon.GetCouponUseCase
import kr.co.cmc.hil.domain.usecase.coupon.GetIncomingCouponRequestUseCase
import kr.co.cmc.hil.domain.usecase.coupon.RequestCouponUseUseCase
import kr.co.cmc.hil.domain.usecase.stamp.GetStampsUseCase
import kr.co.cmc.hil.presentation.common.MviViewModel

internal const val IncomingRequestPollIntervalMillis = 5_000L
internal const val PendingCouponPollIntervalMillis = 3_000L

/**
 * 스탬프 현황, 소원권 교환·사용 요청, 받은 사용 요청을 관리한다.
 *
 * - 화면이 보이는 동안([WishCouponAction.Observe]) 받은 요청을 5초마다, 승인 대기 중인 내 소원권을 3초마다 조회한다.
 * - 교환과 사용 요청은 진행 중이면 중복 요청을 무시한다.
 */
@HiltViewModel
internal class WishCouponViewModel @Inject constructor(
    private val getStamps: GetStampsUseCase,
    private val getCoupon: GetCouponUseCase,
    private val exchangeCoupon: ExchangeCouponUseCase,
    private val requestCouponUse: RequestCouponUseUseCase,
    private val getIncomingCouponRequest: GetIncomingCouponRequestUseCase,
) : MviViewModel<WishCouponUiState, WishCouponAction, WishCouponEvent>(WishCouponUiState()) {

    private var refreshJob: Job? = null

    private var incomingJob: Job? = null

    private var pendingJob: Job? = null

    private var isVisible = false

    private suspend fun loadStatus() {
        val (stamps, coupon) = coroutineScope {
            val stampsResult = async { getStamps() }
            val couponResult = async { getCoupon() }

            stampsResult.await() to couponResult.await()
        }

        if (stamps is AppResult.Success && coupon is AppResult.Success) {
            updateState {
                it.copy(
                    isLoading = false,
                    hasLoadError = false,
                    stamps = stamps.data,
                    coupon = coupon.data,
                )
            }
            startPendingPollingIfNeeded()
        } else {
            val hasLoaded = uiState.value.stamps != null

            updateState { it.copy(isLoading = false, hasLoadError = !hasLoaded) }
            if (hasLoaded) {
                emitEvent(WishCouponEvent.ShowMessage(R.string.wish_coupon_load_failed))
            }
        }
    }

    private fun refresh() {
        if (refreshJob?.isActive == true) {
            // 이미 조회 중이면 같은 결과를 기다린다.
        } else {
            updateState { it.copy(isLoading = it.stamps == null, hasLoadError = false) }
            refreshJob = viewModelScope.launch {
                launch { loadIncomingRequest() }
                loadStatus()
            }
        }
    }

    private suspend fun loadIncomingRequest() {
        val result = getIncomingCouponRequest()

        if (result is AppResult.Success) {
            updateState { it.copy(incomingRequest = result.data) }
        } else {
            // 폴링 중 일시적인 실패는 다음 주기에 다시 조회한다.
        }
    }

    private fun startIncomingPolling() {
        incomingJob?.cancel()
        incomingJob = viewModelScope.launch {
            while (isActive) {
                loadIncomingRequest()
                delay(IncomingRequestPollIntervalMillis)
            }
        }
    }

    private suspend fun applyResolvedCoupon(coupon: Coupon?) {
        val message = if (coupon?.status == CouponStatus.AVAILABLE) {
            R.string.wish_coupon_use_rejected
        } else {
            R.string.wish_coupon_use_approved
        }

        updateState { it.copy(coupon = coupon) }
        emitEvent(WishCouponEvent.ShowMessage(message))
        loadStatus()
    }

    private suspend fun pollPendingCoupon() {
        var isPending = true

        while (isPending) {
            delay(PendingCouponPollIntervalMillis)
            val result = getCoupon()
            if (result is AppResult.Success) {
                isPending = result.data?.status == CouponStatus.PENDING
                if (isPending) {
                    updateState { it.copy(coupon = result.data) }
                } else {
                    applyResolvedCoupon(result.data)
                }
            } else {
                // 일시적인 실패는 다음 주기에 다시 조회한다.
            }
        }
    }

    private fun startPendingPollingIfNeeded() {
        val isPending = uiState.value.activeCoupon?.status == CouponStatus.PENDING

        if (isVisible && isPending && pendingJob?.isActive != true) {
            pendingJob = viewModelScope.launch { pollPendingCoupon() }
        } else if (!isPending) {
            pendingJob?.cancel()
        } else {
            // 화면이 보이지 않거나 이미 폴링 중이다.
        }
    }

    private fun observe(visible: Boolean) {
        isVisible = visible

        if (visible) {
            startIncomingPolling()
            startPendingPollingIfNeeded()
        } else {
            incomingJob?.cancel()
            pendingJob?.cancel()
        }
    }

    private suspend fun executeExchange() {
        val result = exchangeCoupon()

        updateState { it.copy(isExchanging = false) }
        when (result) {
            is AppResult.Success -> {
                loadStatus()
                emitEvent(WishCouponEvent.ExchangeCompleted)
            }
            is AppResult.Error -> {
                val message = if (result.error == AppError.Network.CONFLICT) {
                    R.string.wish_coupon_exchange_conflict
                } else {
                    R.string.wish_coupon_exchange_failed
                }
                emitEvent(WishCouponEvent.ShowMessage(message))
                loadStatus()
            }
        }
    }

    private fun exchange() {
        val state = uiState.value

        if (state.isExchanging || state.buttonState != WishCouponButtonState.Exchangeable) {
            // 진행 중인 교환이 있거나 교환할 수 없는 상태다.
        } else {
            updateState { it.copy(isExchanging = true) }
            viewModelScope.launch { executeExchange() }
        }
    }

    private suspend fun executeRequestUse(coupon: Coupon) {
        val result = requestCouponUse(coupon.couponId)

        when (result) {
            is AppResult.Success -> {
                updateState {
                    it.copy(
                        isRequestingUse = false,
                        hasUseRequestError = false,
                        coupon = coupon.copy(status = CouponStatus.PENDING),
                    )
                }
                emitEvent(WishCouponEvent.UseRequested)
                emitEvent(WishCouponEvent.ShowMessage(R.string.wish_coupon_use_requested))
                startPendingPollingIfNeeded()
            }
            is AppResult.Error -> {
                val message = if (result.error == AppError.Network.CONFLICT) {
                    R.string.wish_coupon_use_conflict
                } else {
                    R.string.wish_coupon_use_failed
                }
                updateState { it.copy(isRequestingUse = false, hasUseRequestError = true) }
                emitEvent(WishCouponEvent.ShowMessage(message))
                if (result.error == AppError.Network.CONFLICT) {
                    loadStatus()
                } else {
                    // 네트워크 실패는 다이얼로그에서 다시 시도한다.
                }
            }
        }
    }

    private fun requestUse() {
        val state = uiState.value
        val coupon = state.activeCoupon?.takeIf { it.status == CouponStatus.AVAILABLE }

        if (state.isRequestingUse || coupon == null) {
            // 진행 중인 요청이 있거나 사용할 수 있는 소원권이 없다.
        } else {
            updateState { it.copy(isRequestingUse = true, hasUseRequestError = false) }
            viewModelScope.launch { executeRequestUse(coupon) }
        }
    }

    override fun onAction(action: WishCouponAction) {
        when (action) {
            WishCouponAction.Refresh -> refresh()
            is WishCouponAction.Observe -> observe(action.isVisible)
            WishCouponAction.Exchange -> exchange()
            WishCouponAction.RequestUse -> requestUse()
            WishCouponAction.ClearUseRequestError -> updateState { it.copy(hasUseRequestError = false) }
        }
    }
}
