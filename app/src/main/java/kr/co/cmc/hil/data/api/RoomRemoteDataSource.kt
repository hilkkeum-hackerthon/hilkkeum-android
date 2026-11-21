package kr.co.cmc.hil.data.api

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kr.co.cmc.hil.BuildConfig
import kr.co.cmc.hil.data.dto.CreateRoomDto
import kr.co.cmc.hil.data.dto.JoinRoomDto
import kr.co.cmc.hil.data.dto.JoinRoomRequest
import kr.co.cmc.hil.data.dto.RoomStatusDto

internal class RoomRemoteDataSource(
    private val baseUrl: String,
    private val openConnection: (URI) -> HttpURLConnection,
) {
    @Inject
    constructor() : this(BuildConfig.ROOM_API_BASE_URL, { uri ->
        uri.toURL().openConnection() as HttpURLConnection
    })

    private val json = Json { ignoreUnknownKeys = true }

    private suspend inline fun <reified T> request(
        path: String,
        method: String,
        expectedStatus: Int,
        token: String? = null,
        body: String? = null,
    ): T = withContext(Dispatchers.IO) {
        val base = runCatching { URI(baseUrl) }.getOrNull()
        if (base?.scheme != "https" || base.host == null || base.userInfo != null ||
            base.query != null || base.fragment != null
        ) {
            throw MissingRoomServerException()
        }

        val connection = openConnection(URI(base.toString().trimEnd('/') + path))
        try {
            connection.requestMethod = method
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("Accept", "application/json")
            if (token != null) connection.setRequestProperty("Authorization", "Bearer $token")
            if (body != null) {
                val bytes = body.toByteArray(Charsets.UTF_8)
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { it.write(bytes) }
            }

            val status = connection.responseCode
            if (status != expectedStatus) {
                connection.errorStream?.close()
                throw RoomHttpException(status)
            }
            val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            json.decodeFromString<T>(response)
        } finally {
            connection.disconnect()
        }
    }

    suspend fun create(): CreateRoomDto = request("/api/v1/rooms", "POST", 201)

    suspend fun join(inviteCode: String): JoinRoomDto =
        request("/api/v1/rooms/join", "POST", 200, body = json.encodeToString(JoinRoomRequest(inviteCode)))

    suspend fun status(roomId: Long, memberToken: String): RoomStatusDto =
        request("/api/v1/rooms/$roomId", "GET", 200, token = memberToken)
}

internal class RoomHttpException(val status: Int) : IOException()
internal class MissingRoomServerException : IOException()
