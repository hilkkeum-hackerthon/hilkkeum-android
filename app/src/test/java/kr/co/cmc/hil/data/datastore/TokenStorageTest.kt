package kr.co.cmc.hil.data.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kr.co.cmc.hil.domain.model.room.RoomId
import kr.co.cmc.hil.domain.model.room.RoomSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TokenStorageTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun savesAndClearsRoomAndMemberTokenTogether() = runBlocking {
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val file = temporaryFolder.newFolder().resolve("test.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        val storage = TokenStorage(dataStore)
        val createdRoom = RoomSession(RoomId(1L), "ABC123")
        val joinedRoom = RoomSession(RoomId(2L), null)

        try {
            storage.saveRoomSession("creator-token", createdRoom)
            assertEquals(createdRoom, TokenStorage(dataStore).readRoomSession())
            assertEquals("creator-token", storage.memberToken.first())

            storage.saveRoomSession("joining-token", joinedRoom)
            assertEquals(joinedRoom, TokenStorage(dataStore).readRoomSession())
            assertEquals("joining-token", storage.memberToken.first())

            storage.clearRoomSession()
            assertNull(storage.readRoomSession())
            assertNull(storage.memberToken.first())
        } finally {
            scope.cancel()
        }
    }
}
