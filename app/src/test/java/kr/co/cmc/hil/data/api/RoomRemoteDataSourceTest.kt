package kr.co.cmc.hil.data.api

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RoomRemoteDataSourceTest {
    @Test
    fun createPostsWithoutABodyAndReadsTheServerInviteCode() = runBlocking {
        val response = FakeConnection(201, """{"roomId":1,"inviteCode":"ABC123","memberToken":"test","status":"PENDING"}""")
        val api = remote("/api/v1/rooms", response)

        val room = api.create()

        assertEquals("POST", response.requestMethod)
        assertFalse(response.doOutput)
        assertEquals("ABC123", room.inviteCode)
    }

    @Test
    fun joinSendsInviteCodeAsJson() = runBlocking {
        val response = FakeConnection(200, """{"roomId":1,"memberToken":"join-test","role":"JOINER","roomStatus":"ACTIVE"}""")
        val api = remote("/api/v1/rooms/join", response)

        val joined = api.join("ABC123")

        assertEquals("POST", response.requestMethod)
        assertEquals("application/json", response.getRequestProperty("Content-Type"))
        assertEquals("""{"inviteCode":"ABC123"}""", response.body.toString(Charsets.UTF_8.name()))
        assertEquals("join-test", joined.memberToken)
    }

    @Test
    fun statusUsesRoomIdAndBearerToken() = runBlocking {
        val response = FakeConnection(200, """{"roomId":7,"status":"ACTIVE","memberCount":2,"capacity":2}""")
        val api = remote("/api/v1/rooms/7", response)

        val status = api.status(7, "status-test")

        assertEquals("GET", response.requestMethod)
        assertEquals("Bearer status-test", response.getRequestProperty("Authorization"))
        assertEquals(2, status.memberCount)
    }

    private fun remote(path: String, connection: FakeConnection) =
        RoomRemoteDataSource("https://example.invalid/") { uri ->
            assertEquals("https://example.invalid$path", uri.toString())
            connection
        }

    private class FakeConnection(private val status: Int, private val response: String) :
        HttpURLConnection(URL("https://example.invalid")) {
        val body = ByteArrayOutputStream()
        override fun getResponseCode() = status
        override fun getOutputStream() = body
        override fun getInputStream() = ByteArrayInputStream(response.toByteArray())
        override fun disconnect() = Unit
        override fun usingProxy() = false
        override fun connect() = Unit
    }
}
