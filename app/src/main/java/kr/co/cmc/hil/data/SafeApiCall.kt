package kr.co.cmc.hil.data

import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException

/**
 * Ktor 요청을 실행하고 예외/HTTP 상태 코드를 [AppError.Network]로 변환한다.
 *
 * 사용 예: `safeApiCall<NoteDto> { httpClient.get("/notes") }`
 */
suspend inline fun <reified T> safeApiCall(
    execute: () -> HttpResponse,
): AppResult<T, AppError.Network> {
    return try {
        responseToResult(execute())
    } catch (e: CancellationException) {
        throw e
    } catch (e: UnknownHostException) {
        AppResult.Error(AppError.Network.NO_INTERNET)
    } catch (e: UnresolvedAddressException) {
        AppResult.Error(AppError.Network.NO_INTERNET)
    } catch (e: HttpRequestTimeoutException) {
        AppResult.Error(AppError.Network.REQUEST_TIMEOUT)
    } catch (e: SocketTimeoutException) {
        AppResult.Error(AppError.Network.REQUEST_TIMEOUT)
    } catch (e: SerializationException) {
        AppResult.Error(AppError.Network.SERIALIZATION)
    } catch (e: Exception) {
        AppResult.Error(AppError.Network.UNKNOWN)
    }
}

/**
 * HTTP 상태 코드에 따라 [AppResult]로 변환한다.
 * 2xx는 본문을 [T]로 파싱해 성공으로, 그 외는 대응하는 [AppError.Network]로 반환한다.
 *
 * public inline 함수인 [safeApiCall]에서 호출하기 위해 `@PublishedApi`로 선언했다.
 */
@PublishedApi
internal suspend inline fun <reified T> responseToResult(
    response: HttpResponse,
): AppResult<T, AppError.Network> {
    return when (response.status.value) {
        in 200..299 -> AppResult.Success(response.body<T>())
        400 -> AppResult.Error(AppError.Network.BAD_REQUEST)
        401 -> AppResult.Error(AppError.Network.UNAUTHORIZED)
        403 -> AppResult.Error(AppError.Network.FORBIDDEN)
        404 -> AppResult.Error(AppError.Network.NOT_FOUND)
        408 -> AppResult.Error(AppError.Network.REQUEST_TIMEOUT)
        409 -> AppResult.Error(AppError.Network.CONFLICT)
        413 -> AppResult.Error(AppError.Network.PAYLOAD_TOO_LARGE)
        429 -> AppResult.Error(AppError.Network.TOO_MANY_REQUESTS)
        503 -> AppResult.Error(AppError.Network.SERVICE_UNAVAILABLE)
        in 500..599 -> AppResult.Error(AppError.Network.SERVER_ERROR)
        else -> AppResult.Error(AppError.Network.UNKNOWN)
    }
}
