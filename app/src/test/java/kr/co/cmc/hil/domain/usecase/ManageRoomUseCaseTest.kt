package kr.co.cmc.hil.domain.usecase

import kotlinx.coroutines.runBlocking
import kr.co.cmc.hil.domain.model.RoomError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.RoomSession
import kr.co.cmc.hil.domain.model.RoomStatus
import kr.co.cmc.hil.domain.repository.RoomRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ManageRoomUseCaseTest {
    @Test
    fun invalidCodeIsRejectedWithoutCallingRepository() = runBlocking {
        val repository = FakeRoomRepository()
        val rooms = ManageRoomUseCase(repository)

        assertEquals(AppResult.Error(RoomError.ValidationFailed), rooms.join("ABC12"))
        assertNull(repository.joinedCode)
    }

    @Test
    fun joinTrimsOuterWhitespaceAndPreservesTheCode() = runBlocking {
        val repository = FakeRoomRepository()
        val rooms = ManageRoomUseCase(repository)

        rooms.join(" AbC123 ")

        assertEquals("AbC123", repository.joinedCode)
    }

    @Test
    fun pairingRequiresTwoMembersAndAnActiveRoom() {
        assertFalse(RoomStatus(1, "PENDING", 1).isPaired)
        assertFalse(RoomStatus(1, "ACTIVE", 1).isPaired)
        assertFalse(RoomStatus(1, "PENDING", 2).isPaired)
        assertTrue(RoomStatus(1, "ACTIVE", 2).isPaired)
    }

    private class FakeRoomRepository : RoomRepository {
        var joinedCode: String? = null
            private set

        override suspend fun join(inviteCode: String): AppResult<RoomSession, RoomError> {
            joinedCode = inviteCode
            return AppResult.Success(RoomSession(1, "fake-token", inviteCode, false))
        }

        override suspend fun create(): AppResult<RoomSession, RoomError> = error("Not used")
        override suspend fun status(session: RoomSession): AppResult<RoomStatus, RoomError> = error("Not used")
        override suspend fun restore(): AppResult<RoomSession?, RoomError> = error("Not used")
        override suspend fun save(session: RoomSession): AppResult<Unit, RoomError> = error("Not used")
        override suspend fun clear(): AppResult<Unit, RoomError> = error("Not used")
    }
}
