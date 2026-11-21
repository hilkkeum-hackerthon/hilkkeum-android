package kr.co.cmc.hil.presentation.coupon.approval

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.Coupon
import kr.co.cmc.hil.domain.model.coupon.CouponId
import kr.co.cmc.hil.domain.model.coupon.CouponUseRequest
import kr.co.cmc.hil.domain.model.coupon.CouponUseResponse
import kr.co.cmc.hil.domain.model.coupon.ExchangedCoupon
import kr.co.cmc.hil.domain.model.coupon.IncomingCouponRequest
import kr.co.cmc.hil.domain.repository.CouponRepository
import kr.co.cmc.hil.domain.usecase.coupon.RespondCouponUseUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CouponApprovalViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val repository = FakeCouponRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun approveShowsApprovedResultAndEmitsResponded() = runTest(dispatcher) {
        val viewModel = CouponApprovalViewModel(RespondCouponUseUseCase(repository))
        val events = mutableListOf<CouponApprovalEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.toList(events) }

        viewModel.onAction(CouponApprovalAction.Start(1))
        viewModel.onAction(CouponApprovalAction.Respond(approved = true))
        viewModel.onAction(CouponApprovalAction.Respond(approved = true))
        advanceUntilIdle()

        assertEquals(CouponApprovalResult.Approved, viewModel.uiState.value.result)
        assertEquals(listOf(true), repository.responses)
        assertTrue(CouponApprovalEvent.Responded in events)
    }

    @Test
    fun rejectShowsRejectedResult() = runTest(dispatcher) {
        val viewModel = CouponApprovalViewModel(RespondCouponUseUseCase(repository))

        viewModel.onAction(CouponApprovalAction.Start(1))
        viewModel.onAction(CouponApprovalAction.Respond(approved = false))
        advanceUntilIdle()

        assertEquals(CouponApprovalResult.Rejected, viewModel.uiState.value.result)
    }

    @Test
    fun conflictMarksRequestUnavailable() = runTest(dispatcher) {
        repository.failure = AppError.Network.CONFLICT
        val viewModel = CouponApprovalViewModel(RespondCouponUseUseCase(repository))

        viewModel.onAction(CouponApprovalAction.Start(1))
        viewModel.onAction(CouponApprovalAction.Respond(approved = true))
        advanceUntilIdle()

        assertEquals(CouponApprovalResult.Unavailable, viewModel.uiState.value.result)
    }

    @Test
    fun networkFailureAllowsRetry() = runTest(dispatcher) {
        repository.failure = AppError.Network.NO_INTERNET
        val viewModel = CouponApprovalViewModel(RespondCouponUseUseCase(repository))

        viewModel.onAction(CouponApprovalAction.Start(1))
        viewModel.onAction(CouponApprovalAction.Respond(approved = true))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasError)
        assertTrue(viewModel.uiState.value.canRespond)

        repository.failure = null
        viewModel.onAction(CouponApprovalAction.Respond(approved = true))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasError)
        assertEquals(CouponApprovalResult.Approved, viewModel.uiState.value.result)
    }

    @Test
    fun startingAnotherCouponClearsPreviousResult() = runTest(dispatcher) {
        val viewModel = CouponApprovalViewModel(RespondCouponUseUseCase(repository))
        viewModel.onAction(CouponApprovalAction.Start(1))
        viewModel.onAction(CouponApprovalAction.Respond(approved = true))
        advanceUntilIdle()

        viewModel.onAction(CouponApprovalAction.Start(2))

        assertEquals(CouponApprovalUiState(couponId = 2), viewModel.uiState.value)
    }

    private class FakeCouponRepository : CouponRepository {
        var failure: AppError.Network? = null

        val responses = mutableListOf<Boolean>()

        override suspend fun exchangeCoupon(): AppResult<ExchangedCoupon, AppError.Network> = error("unused")

        override suspend fun getCoupon(): AppResult<Coupon?, AppError.Network> = error("unused")

        override suspend fun requestCouponUse(couponId: CouponId): AppResult<CouponUseRequest, AppError.Network> =
            error("unused")

        override suspend fun respondCouponUse(
            couponId: CouponId,
            approved: Boolean,
        ): AppResult<CouponUseResponse, AppError.Network> {
            val current = failure
            return if (current != null) {
                AppResult.Error(current)
            } else {
                responses += approved
                AppResult.Success(CouponUseResponse(couponId, approved, null))
            }
        }

        override suspend fun getIncomingCouponRequest(): AppResult<IncomingCouponRequest?, AppError.Network> =
            error("unused")
    }
}
