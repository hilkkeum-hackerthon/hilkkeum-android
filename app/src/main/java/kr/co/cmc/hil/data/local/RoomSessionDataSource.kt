package kr.co.cmc.hil.data.local

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileNotFoundException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.co.cmc.hil.domain.model.RoomSession

@Singleton
internal class RoomSessionDataSource @Inject constructor(@ApplicationContext context: Context) {
    private val file = AtomicFile(File(context.noBackupFilesDir, "room-session"))
    private val keyAlias = "hilkkeum.room-session"
    private val keyStoreName = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"
    private val ivLength = 12

    private fun key(create: Boolean): SecretKey {
        val store = KeyStore.getInstance(keyStoreName).apply { load(null) }
        val existing = store.getKey(keyAlias, null) as? SecretKey
        return existing ?: if (create) {
            val spec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, keyStoreName).apply {
                init(spec)
            }.generateKey()
        } else {
            error("Room session key is unavailable")
        }
    }

    @Synchronized
    fun load(): RoomSession? {
        val bytes = try {
            file.readFully()
        } catch (_: FileNotFoundException) {
            return null
        }
        require(bytes.size > ivLength)
        val cipher = Cipher.getInstance(transformation)
        val parameters = GCMParameterSpec(128, bytes.copyOfRange(0, ivLength))
        cipher.init(Cipher.DECRYPT_MODE, key(create = false), parameters)
        val decoded = cipher.doFinal(bytes, ivLength, bytes.size - ivLength)
        val session = Json.decodeFromString<StoredSession>(decoded.toString(Charsets.UTF_8))
        return RoomSession(session.roomId, session.token, session.code, session.isCreator)
    }

    @Synchronized
    fun save(session: RoomSession) {
        val cipher = Cipher.getInstance(transformation)
        val value = StoredSession(session.roomId, session.memberToken, session.inviteCode, session.isCreator)
        cipher.init(Cipher.ENCRYPT_MODE, key(create = true))
        check(cipher.iv.size == ivLength)
        val bytes = cipher.iv + cipher.doFinal(Json.encodeToString(value).toByteArray(Charsets.UTF_8))
        val stream = file.startWrite()
        try {
            stream.write(bytes)
            file.finishWrite(stream)
        } catch (error: Exception) {
            file.failWrite(stream)
            throw error
        }
    }

    @Synchronized
    fun clear() = file.delete()

    @Serializable
    private data class StoredSession(val roomId: Long, val token: String, val code: String, val isCreator: Boolean)
}
