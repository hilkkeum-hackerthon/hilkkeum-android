package kr.co.cmc.hil.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
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

    suspend fun saveMemberToken(token: String) {
        dataStore.edit { preferences ->
            preferences[MEMBER_TOKEN_KEY] = token
        }
    }

    suspend fun clearMemberToken() {
        dataStore.edit { preferences ->
            preferences.remove(MEMBER_TOKEN_KEY)
        }
    }

    private companion object {
        val MEMBER_TOKEN_KEY = stringPreferencesKey("memberToken")
    }
}
