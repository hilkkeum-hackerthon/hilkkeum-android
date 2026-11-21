package kr.co.cmc.hil.data.repositoryimpl

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kr.co.cmc.hil.data.datastore.TokenStorage
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.coupon.CouponId
import kr.co.cmc.hil.domain.model.coupon.CouponStatus
import kr.co.cmc.hil.domain.model.room.RoomId
import kr.co.cmc.hil.domain.model.room.RoomSession
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * `docs/API명세서.md` 21–24번 응답 예시로 [CouponRepositoryImpl]을 검증한다.
 */
class CouponRepositoryImplTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var tokenStorage: TokenStorage

    private fun createTokenStorage(): TokenStorage {
        val file = temporaryFolder.newFolder().resolve("test.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }

        return TokenStorage(dataStore)
    }

    private val requests = mutableListOf<HttpRequestData>()

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK): HttpResponseData =
        respond(body, status, headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))

    private fun repository(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): CouponRepositoryImpl {
        val engine = MockEngine { request ->
            requests += request
            handler(request)
        }
        val client = HttpClient(engine) {
            defaultRequest {
                url("https://example.test/")
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        return CouponRepositoryImpl(client, tokenStorage)
    }

    @Before
    fun setUp() = runBlocking {
        tokenStorage = createTokenStorage()
        tokenStorage.saveRoomSession("member-token", RoomSession(RoomId(1), null))
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun exchangeReturnsIssuedCouponAndRemainingStamps() = runBlocking {
        val repository = repository {
            json(
                """{"couponId":1,"memberId":1,"heartCount":0,"coupon":{"status":"AVAILABLE",""" +
                    """"issuedAt":"2026-11-21T20:00:00+09:00","expiresAt":"2026-12-21T20:00:00+09:00"}}""",
                HttpStatusCode.Created,
            )
        }

        val result = repository.exchangeCoupon()

        assertTrue(result is AppResult.Success)
        val coupon = (result as AppResult.Success).data
        assertEquals(CouponId(1), coupon.couponId)
        assertEquals(0, coupon.heartCount)
        assertEquals(HttpMethod.Post, requests.single().method)
        assertEquals("/api/v1/coupons/exchange", requests.single().url.encodedPath)
        assertEquals("Bearer member-token", requests.single().headers[HttpHeaders.Authorization])
    }

    @Test
    fun exchangeConflictMapsToConflict() = runBlocking {
        val repository = repository { json("""{"message":"소원권 보유 중"}""", HttpStatusCode.Conflict) }

        assertEquals(AppResult.Error(AppError.Network.CONFLICT), repository.exchangeCoupon())
    }

    @Test
    fun couponStatusMovesFromAvailableToPendingToUsedAndBackOnReject() = runBlocking {
        val statuses = ArrayDeque(listOf("AVAILABLE", "PENDING", "USED", "AVAILABLE"))
        val repository = repository {
            json(
                """{"hasActiveCoupon":true,"coupon":{"couponId":1,"status":"${statuses.removeFirst()}",""" +
                    """"issuedAt":"2026-11-21T20:00:00+09:00","expiresAt":"2026-12-21T20:00:00+09:00","remainingDays":30}}""",
            )
        }

        val observed = List(4) { (repository.getCoupon() as AppResult.Success).data?.status }

        assertEquals(
            listOf(CouponStatus.AVAILABLE, CouponStatus.PENDING, CouponStatus.USED, CouponStatus.AVAILABLE),
            observed,
        )
    }

    @Test
    fun couponIsNullWhenNoneHeld() = runBlocking {
        val repository = repository { json("""{"hasActiveCoupon":false,"coupon":null}""") }

        assertEquals(AppResult.Success(null), repository.getCoupon())
    }

    @Test
    fun couponRemainingDaysUsesServerValue() = runBlocking {
        val repository = repository {
            json(
                """{"hasActiveCoupon":true,"coupon":{"couponId":7,"status":"AVAILABLE",""" +
                    """"issuedAt":"2026-11-21T20:00:00+09:00","expiresAt":"2026-12-21T20:00:00+09:00","remainingDays":12}}""",
            )
        }

        val coupon = (repository.getCoupon() as AppResult.Success).data

        assertEquals(12, coupon?.remainingDays)
    }

    @Test
    fun useCouponCompletesImmediately() = runBlocking {
        val repository = repository {
            json("""{"couponId":1,"memberId":1,"status":"USED"}""")
        }

        val result = repository.requestCouponUse(CouponId(1))

        assertEquals(false, (result as AppResult.Success).data.waitingForPartner)
        assertEquals("/api/v1/coupons/1/use", requests.single().url.encodedPath)
    }

    @Test
    fun requestUseConflictAndForbiddenAreMapped() = runBlocking {
        val codes = ArrayDeque(listOf(HttpStatusCode.Conflict, HttpStatusCode.Forbidden))
        val repository = repository { json("{}", codes.removeFirst()) }

        assertEquals(AppResult.Error(AppError.Network.CONFLICT), repository.requestCouponUse(CouponId(1)))
        assertEquals(AppResult.Error(AppError.Network.FORBIDDEN), repository.requestCouponUse(CouponId(1)))
    }

    @Test
    fun respondApprovedAndRejected() = runBlocking {
        val bodies = ArrayDeque(
            listOf(
                """{"couponId":1,"approved":true,"usedAt":"2026-11-22T10:00:00+09:00"}""",
                """{"couponId":1,"approved":false,"usedAt":null}""",
            ),
        )
        val repository = repository { json(bodies.removeFirst()) }

        val approved = (repository.respondCouponUse(CouponId(1), true) as AppResult.Success).data
        val rejected = (repository.respondCouponUse(CouponId(1), false) as AppResult.Success).data

        assertTrue(approved.approved)
        assertTrue(approved.usedAt != null)
        assertEquals(false, rejected.approved)
        assertNull(rejected.usedAt)
        assertEquals("/api/v1/coupons/1/respond", requests.first().url.encodedPath)
    }

    @Test
    fun incomingRequestIsParsed() = runBlocking {
        val repository = repository { json("""{"couponId":3,"requestedAt":"2026-11-22T10:00:00+09:00"}""") }

        val request = (repository.getIncomingCouponRequest() as AppResult.Success).data

        assertEquals(CouponId(3), request?.couponId)
        assertEquals("/api/v1/coupons/incoming", requests.single().url.encodedPath)
    }

    @Test
    fun incomingRequestIsNullForNullBodyOrMissingEndpoint() = runBlocking {
        val responses = ArrayDeque(listOf("null" to HttpStatusCode.OK, "{}" to HttpStatusCode.NotFound))
        val repository = repository {
            val (body, status) = responses.removeFirst()
            json(body, status)
        }

        assertEquals(AppResult.Success(null), repository.getIncomingCouponRequest())
        assertEquals(AppResult.Success(null), repository.getIncomingCouponRequest())
    }

    @Test
    fun missingTokenFailsWithoutRequest() = runBlocking {
        // 저장된 토큰이 없는 새 저장소를 쓴다.
        tokenStorage = createTokenStorage()
        val repository = repository { json("{}") }

        assertEquals(AppResult.Error(AppError.Network.UNAUTHORIZED), repository.getCoupon())
        assertTrue(requests.isEmpty())
    }
}
