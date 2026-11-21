package kr.co.cmc.hil.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "token_storage")

/**
 * 회원 토큰을 DataStore(Preferences)에 저장/조회한다.
 */
@Singleton
class TokenStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val memberToken: Flow<String?> = context.tokenDataStore.data.map { preferences ->
        preferences[MEMBER_TOKEN_KEY]
    }

    suspend fun saveMemberToken(token: String) {
        context.tokenDataStore.edit { preferences ->
            preferences[MEMBER_TOKEN_KEY] = token
        }
    }

    suspend fun clearMemberToken() {
        context.tokenDataStore.edit { preferences ->
            preferences.remove(MEMBER_TOKEN_KEY)
        }
    }

    private companion object {
        val MEMBER_TOKEN_KEY = stringPreferencesKey("memberToken")
    }
}
