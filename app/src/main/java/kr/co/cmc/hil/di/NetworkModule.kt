package kr.co.cmc.hil.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kr.co.cmc.hil.BuildConfig
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
    fun provideHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        defaultRequest {
            url(BuildConfig.BASE_URL)
            contentType(ContentType.Application.Json)
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
