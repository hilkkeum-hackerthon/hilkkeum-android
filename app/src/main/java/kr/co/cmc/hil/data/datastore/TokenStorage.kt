package kr.co.cmc.hil.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kr.co.cmc.hil.domain.model.room.RoomId
import kr.co.cmc.hil.domain.model.room.RoomSession
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 회원 토큰을 DataStore(Preferences)에 저장/조회한다.
 */
@Singleton
class TokenStorage @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val memberToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[MEMBER_TOKEN_KEY]
    }

    suspend fun readRoomSession(): RoomSession? {
        val preferences = dataStore.data.first()
        if (preferences[MEMBER_TOKEN_KEY].isNullOrBlank()) return null
        val roomId = preferences[ROOM_ID_KEY] ?: return null
        return RoomSession(RoomId(roomId), preferences[INVITE_CODE_KEY])
    }

    suspend fun saveRoomSession(token: String, session: RoomSession) {
        dataStore.edit { preferences ->
            preferences[MEMBER_TOKEN_KEY] = token
            preferences[ROOM_ID_KEY] = session.roomId.value
            val inviteCode = session.inviteCode
            if (inviteCode == null) preferences.remove(INVITE_CODE_KEY)
            else preferences[INVITE_CODE_KEY] = inviteCode
        }
    }

    suspend fun clearRoomSession() {
        dataStore.edit { preferences ->
            preferences.remove(MEMBER_TOKEN_KEY)
            preferences.remove(ROOM_ID_KEY)
            preferences.remove(INVITE_CODE_KEY)
        }
    }

    private companion object {
        val ROOM_ID_KEY = longPreferencesKey("roomId")
        val INVITE_CODE_KEY = stringPreferencesKey("inviteCode")
        val MEMBER_TOKEN_KEY = stringPreferencesKey("memberToken")
    }
}
