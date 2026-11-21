package kr.co.cmc.hil.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kr.co.cmc.hil.BuildConfig
import kr.co.cmc.hil.data.datastore.TokenStorage
import javax.inject.Singleton

/**
 * Ktor [HttpClient]를 제공한다.
 *
 * - 요청 경로는 [BuildConfig.BASE_URL] 기준 상대 경로로 작성한다. (예: `httpClient.get("notes")`)
 * - `expectSuccess`는 기본값(false)을 유지해 4xx/5xx 응답도 `safeApiCall`이 [kr.co.cmc.hil.domain.common.AppError]로 변환하도록 한다.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_MILLIS = 15_000L
    private const val LOG_TAG = "HttpClient"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json, tokenStorage: TokenStorage): HttpClient = HttpClient(OkHttp) {
        defaultRequest {
            url(BuildConfig.BASE_URL)
            contentType(ContentType.Application.Json)
        }

        // 토큰은 한 번 발급되면 바뀌지 않는다. 저장되기 전(null)에는 요청마다 다시 읽고, 저장된 뒤에는 캐시된 값을 쓴다.
        install(Auth) {
            bearer {
                loadTokens {
                    tokenStorage.memberToken.first()?.let { BearerTokens(accessToken = it, refreshToken = null) }
                }
            }
        }

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MILLIS
            connectTimeoutMillis = TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Log.d(LOG_TAG, message)
                }
            }
            level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }
}
