package kr.co.cmc.hil.presentation.stamp

import java.time.OffsetDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.Coupon
import kr.co.cmc.hil.domain.model.coupon.CouponId
import kr.co.cmc.hil.domain.model.coupon.CouponStatus
import kr.co.cmc.hil.domain.model.coupon.CouponUseRequest
import kr.co.cmc.hil.domain.model.coupon.CouponUseResponse
import kr.co.cmc.hil.domain.model.coupon.ExchangedCoupon
import kr.co.cmc.hil.domain.model.coupon.IncomingCouponRequest
import kr.co.cmc.hil.domain.model.room.MemberId
import kr.co.cmc.hil.domain.model.stamp.Stamps
import kr.co.cmc.hil.domain.repository.CouponRepository
import kr.co.cmc.hil.domain.repository.StampRepository
import kr.co.cmc.hil.domain.usecase.coupon.ExchangeCouponUseCase
import kr.co.cmc.hil.domain.usecase.coupon.GetCouponUseCase
import kr.co.cmc.hil.domain.usecase.coupon.GetIncomingCouponRequestUseCase
import kr.co.cmc.hil.domain.usecase.coupon.RequestCouponUseUseCase
import kr.co.cmc.hil.domain.usecase.stamp.GetStampsUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WishCouponViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val issuedAt = OffsetDateTime.parse("2026-11-21T20:00:00+09:00")

    private val stampRepository = FakeStampRepository()

    private val couponRepository = FakeCouponRepository()

    private fun stamps(heartCount: Int, hasActiveCoupon: Boolean = false) = Stamps(
        memberId = MemberId(1),
        heartCount = heartCount,
        maxHearts = 3,
        canExchange = heartCount == 3 && !hasActiveCoupon,
        hasActiveCoupon = hasActiveCoupon,
    )

    private fun coupon(status: CouponStatus) = Coupon(CouponId(1), status, issuedAt, issuedAt.plusMonths(1), 30)

    private fun viewModel() = WishCouponViewModel(
        getStamps = GetStampsUseCase(stampRepository),
        getCoupon = GetCouponUseCase(couponRepository),
        exchangeCoupon = ExchangeCouponUseCase(couponRepository),
        requestCouponUse = RequestCouponUseUseCase(couponRepository),
        getIncomingCouponRequest = GetIncomingCouponRequestUseCase(couponRepository),
    )

    private fun TestScope.collectEvents(viewModel: WishCouponViewModel): MutableList<WishCouponEvent> {
        val events = mutableListOf<WishCouponEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.toList(events) }
        return events
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshShowsStampCountAndLockedButton() = runTest(dispatcher) {
        stampRepository.stamps = stamps(2)
        val viewModel = viewModel()

        viewModel.onAction(WishCouponAction.Refresh)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.heartCount)
        assertEquals(WishCouponButtonState.Locked, state.buttonState)
        assertEquals(0, state.availableCouponCount)
    }

    @Test
    fun refreshFailureWithoutDataShowsLoadError() = runTest(dispatcher) {
        stampRepository.result = AppResult.Error(AppError.Network.NO_INTERNET)
        val viewModel = viewModel()

        viewModel.onAction(WishCouponAction.Refresh)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasLoadError)
    }

    @Test
    fun exchangeIssuesCouponAndOpensTicket() = runTest(dispatcher) {
        stampRepository.stamps = stamps(3)
        val viewModel = viewModel()
        val events = collectEvents(viewModel)
        viewModel.onAction(WishCouponAction.Refresh)
        advanceUntilIdle()
        assertEquals(WishCouponButtonState.Exchangeable, viewModel.uiState.value.buttonState)

        stampRepository.stamps = stamps(0, hasActiveCoupon = true)
        couponRepository.coupons += coupon(CouponStatus.AVAILABLE)
        viewModel.onAction(WishCouponAction.Exchange)
        advanceUntilIdle()

        assertEquals(1, couponRepository.exchangeCount)
        assertEquals(WishCouponButtonState.Usable, viewModel.uiState.value.buttonState)
        assertEquals(1, viewModel.uiState.value.availableCouponCount)
        assertTrue(WishCouponEvent.ExchangeCompleted in events)
    }

    @Test
    fun exchangeConflictShowsConflictMessage() = runTest(dispatcher) {
        stampRepository.stamps = stamps(3)
        couponRepository.exchangeResult = AppResult.Error(AppError.Network.CONFLICT)
        val viewModel = viewModel()
        val events = collectEvents(viewModel)
        viewModel.onAction(WishCouponAction.Refresh)
        advanceUntilIdle()

        viewModel.onAction(WishCouponAction.Exchange)
        advanceUntilIdle()

        assertTrue(WishCouponEvent.ShowMessage(R.string.wish_coupon_exchange_conflict) in events)
        assertFalse(viewModel.uiState.value.isExchanging)
    }

    @Test
    fun immediateUseRefreshesCouponAndStampState() = runTest(dispatcher) {
        stampRepository.stamps = stamps(0, hasActiveCoupon = true)
        couponRepository.coupons += coupon(CouponStatus.AVAILABLE)
        val viewModel = viewModel()
        val events = collectEvents(viewModel)
        viewModel.onAction(WishCouponAction.Refresh)
        runCurrent()

        couponRepository.requestUseResult = AppResult.Success(
            CouponUseRequest(CouponId(1), MemberId(1), waitingForPartner = false),
        )
        couponRepository.coupons.clear()
        stampRepository.stamps = stamps(0)
        viewModel.onAction(WishCouponAction.RequestUse)
        runCurrent()

        assertEquals(WishCouponButtonState.Locked, viewModel.uiState.value.buttonState)
        assertTrue(WishCouponEvent.ShowMessage(R.string.wish_ticket_used) in events)
    }

    @Test
    fun requestUseMovesToPendingAndApprovalIsDetectedByPolling() = runTest(dispatcher) {
        stampRepository.stamps = stamps(0, hasActiveCoupon = true)
        couponRepository.coupons += coupon(CouponStatus.AVAILABLE)
        val viewModel = viewModel()
        val events = collectEvents(viewModel)
        viewModel.onAction(WishCouponAction.Observe(true))
        viewModel.onAction(WishCouponAction.Refresh)
        runCurrent()

        couponRepository.coupons.clear()
        couponRepository.coupons += coupon(CouponStatus.PENDING)
        viewModel.onAction(WishCouponAction.RequestUse)
        runCurrent()

        assertEquals(WishCouponButtonState.Pending, viewModel.uiState.value.buttonState)
        assertTrue(WishCouponEvent.UseRequested in events)

        couponRepository.coupons.clear()
        couponRepository.coupons += null
        stampRepository.stamps = stamps(0)
        advanceTimeBy(PendingCouponPollIntervalMillis * 2)
        runCurrent()

        assertEquals(WishCouponButtonState.Locked, viewModel.uiState.value.buttonState)
        assertTrue(WishCouponEvent.ShowMessage(R.string.wish_coupon_use_approved) in events)
        viewModel.onAction(WishCouponAction.Observe(false))
    }

    @Test
    fun rejectedRequestReturnsCouponToAvailable() = runTest(dispatcher) {
        stampRepository.stamps = stamps(0, hasActiveCoupon = true)
        couponRepository.coupons += coupon(CouponStatus.PENDING)
        val viewModel = viewModel()
        val events = collectEvents(viewModel)
        viewModel.onAction(WishCouponAction.Observe(true))
        viewModel.onAction(WishCouponAction.Refresh)
        runCurrent()

        couponRepository.coupons.clear()
        couponRepository.coupons += coupon(CouponStatus.AVAILABLE)
        advanceTimeBy(PendingCouponPollIntervalMillis * 2)
        runCurrent()

        assertEquals(WishCouponButtonState.Usable, viewModel.uiState.value.buttonState)
        assertTrue(WishCouponEvent.ShowMessage(R.string.wish_coupon_use_rejected) in events)
        viewModel.onAction(WishCouponAction.Observe(false))
    }

    @Test
    fun requestUseFailureKeepsCouponAndMarksError() = runTest(dispatcher) {
        stampRepository.stamps = stamps(0, hasActiveCoupon = true)
        couponRepository.coupons += coupon(CouponStatus.AVAILABLE)
        couponRepository.requestUseResult = AppResult.Error(AppError.Network.NO_INTERNET)
        val viewModel = viewModel()
        viewModel.onAction(WishCouponAction.Refresh)
        advanceUntilIdle()

        viewModel.onAction(WishCouponAction.RequestUse)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasUseRequestError)
        assertEquals(WishCouponButtonState.Usable, viewModel.uiState.value.buttonState)
    }

    @Test
    fun observingPollsIncomingRequestUntilStopped() = runTest(dispatcher) {
        stampRepository.stamps = stamps(1)
        val viewModel = viewModel()

        viewModel.onAction(WishCouponAction.Observe(true))
        runCurrent()
        assertFalse(viewModel.uiState.value.hasIncomingRequest)

        couponRepository.incoming = IncomingCouponRequest(CouponId(5), null)
        advanceTimeBy(IncomingRequestPollIntervalMillis)
        runCurrent()
        assertTrue(viewModel.uiState.value.hasIncomingRequest)

        val callsBeforeStop = couponRepository.incomingCalls
        viewModel.onAction(WishCouponAction.Observe(false))
        advanceTimeBy(IncomingRequestPollIntervalMillis * 3)
        runCurrent()
        assertEquals(callsBeforeStop, couponRepository.incomingCalls)
    }

    @Test
    fun fullStampsWithCouponShowsWarning() = runTest(dispatcher) {
        stampRepository.stamps = stamps(3, hasActiveCoupon = true)
        couponRepository.coupons += coupon(CouponStatus.AVAILABLE)
        val viewModel = viewModel()

        viewModel.onAction(WishCouponAction.Refresh)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showFullWarning)
        assertEquals(WishCouponButtonState.Usable, viewModel.uiState.value.buttonState)
    }

    private class FakeStampRepository : StampRepository {
        var stamps: Stamps? = null

        var result: AppResult<Stamps, AppError.Network>? = null

        override suspend fun getStamps(): AppResult<Stamps, AppError.Network> =
            result ?: AppResult.Success(requireNotNull(stamps))
    }

    private class FakeCouponRepository : CouponRepository {
        /** 조회할 때마다 앞에서부터 꺼내고, 마지막 값은 계속 돌려준다. */
        val coupons = mutableListOf<Coupon?>()

        var exchangeResult: AppResult<ExchangedCoupon, AppError.Network>? = null

        var requestUseResult: AppResult<CouponUseRequest, AppError.Network>? = null

        var incoming: IncomingCouponRequest? = null

        var exchangeCount = 0

        var incomingCalls = 0

        override suspend fun exchangeCoupon(): AppResult<ExchangedCoupon, AppError.Network> {
            exchangeCount++
            val now = OffsetDateTime.parse("2026-11-21T20:00:00+09:00")
            return exchangeResult ?: AppResult.Success(ExchangedCoupon(CouponId(1), MemberId(1), 0, now, now.plusMonths(1)))
        }

        override suspend fun getCoupon(): AppResult<Coupon?, AppError.Network> =
            AppResult.Success(if (coupons.size > 1) coupons.removeAt(0) else coupons.firstOrNull())

        override suspend fun requestCouponUse(couponId: CouponId): AppResult<CouponUseRequest, AppError.Network> =
            requestUseResult ?: AppResult.Success(CouponUseRequest(couponId, MemberId(1), waitingForPartner = true))

        override suspend fun respondCouponUse(
            couponId: CouponId,
            approved: Boolean,
        ): AppResult<CouponUseResponse, AppError.Network> = AppResult.Success(CouponUseResponse(couponId, approved, null))

        override suspend fun getIncomingCouponRequest(): AppResult<IncomingCouponRequest?, AppError.Network> {
            incomingCalls++
            return AppResult.Success(incoming)
        }
    }
}
