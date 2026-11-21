package kr.co.cmc.hil.presentation.invitation

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
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.room.CreatedRoom
import kr.co.cmc.hil.domain.model.room.JoinedRoom
import kr.co.cmc.hil.domain.model.room.Room
import kr.co.cmc.hil.domain.model.room.RoomId
import kr.co.cmc.hil.domain.model.room.RoomSession
import kr.co.cmc.hil.domain.repository.RoomRepository
import kr.co.cmc.hil.domain.usecase.room.CreateRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.DeleteRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.GetRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.JoinRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.RoomSessionUseCase
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomEntryViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val repository = FakeRoomRepository()

    private fun viewModel() = RoomEntryViewModel(
        createRoom = CreateRoomUseCase(repository),
        joinRoom = JoinRoomUseCase(repository),
        getRoom = GetRoomUseCase(repository),
        deleteRoom = DeleteRoomUseCase(repository),
        roomSession = RoomSessionUseCase(repository),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun leavingWaitingRoomDeletesRoomAndClearsSession() = runTest(dispatcher) {
        repository.session = RoomSession(RoomId(7), "ABC123")
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onAction(RoomEntryAction.Leave)
        viewModel.onAction(RoomEntryAction.Leave)
        advanceUntilIdle()

        assertEquals(listOf(RoomId(7)), repository.deletedRooms)
        assertNull(viewModel.uiState.value.session)
        assertFalse(viewModel.uiState.value.isLeaving)
    }

    @Test
    fun leaveFailureKeepsSessionAndShowsMessage() = runTest(dispatcher) {
        repository.session = RoomSession(RoomId(7), "ABC123")
        repository.deleteResult = AppResult.Error(AppError.Network.UNKNOWN)
        val viewModel = viewModel()
        val events = mutableListOf<Int>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.event.toList(events) }
        advanceUntilIdle()

        viewModel.onAction(RoomEntryAction.Leave)
        advanceUntilIdle()

        assertEquals(RoomSession(RoomId(7), "ABC123"), viewModel.uiState.value.session)
        assertTrue(R.string.room_leave_failed in events)
    }

    @Test
    fun leaveIsIgnoredForJoinedMember() = runTest(dispatcher) {
        repository.session = RoomSession(RoomId(7), null)
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onAction(RoomEntryAction.Leave)
        advanceUntilIdle()

        assertTrue(repository.deletedRooms.isEmpty())
        assertEquals(RoomSession(RoomId(7), null), viewModel.uiState.value.session)
    }

    private class FakeRoomRepository : RoomRepository {
        var session: RoomSession? = null

        var deleteResult: AppResult<Unit, AppError.Network> = AppResult.Success(Unit)

        val deletedRooms = mutableListOf<RoomId>()

        override suspend fun restoreSession(): AppResult<RoomSession?, AppError.Network> = AppResult.Success(session)

        override suspend fun clearSession(): AppResult<Unit, AppError.Network> {
            session = null
            return AppResult.Success(Unit)
        }

        override suspend fun createRoom(): AppResult<CreatedRoom, AppError.Network> =
            AppResult.Error(AppError.Network.UNKNOWN)

        override suspend fun joinRoom(inviteCode: String): AppResult<JoinedRoom, AppError.Network> =
            AppResult.Error(AppError.Network.UNKNOWN)

        override suspend fun getRoom(roomId: RoomId): AppResult<Room, AppError.Network> =
            AppResult.Error(AppError.Network.UNKNOWN)

        override suspend fun deleteRoom(roomId: RoomId): AppResult<Unit, AppError.Network> {
            deletedRooms += roomId
            if (deleteResult is AppResult.Success) {
                session = null
            }
            return deleteResult
        }
    }
}
