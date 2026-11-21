package kr.co.cmc.hil.data.datastore

import kotlinx.coroutines.flow.first
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import java.io.IOException

/**
 * 저장된 회원 토큰을 읽어 [request]에 넘긴다. 인증이 필요한 요청에서 `bearerAuth(token)`을 직접 붙일 때 쓴다.
 *
 * Ktor Bearer 플러그인은 서버가 401로 인증을 요구하기 전에는 토큰을 보내지 않으므로, 요청마다 토큰을 명시적으로 붙인다.
 * 토큰을 읽지 못하면 [AppError.Network.UNKNOWN], 저장된 토큰이 없으면 [AppError.Network.UNAUTHORIZED]로 실패한다.
 */
suspend fun <T> TokenStorage.withMemberToken(
    request: suspend (String) -> AppResult<T, AppError.Network>,
): AppResult<T, AppError.Network> {
    val token = try {
        memberToken.first()
    } catch (_: IOException) {
        return AppResult.Error(AppError.Network.UNKNOWN)
    }
    return if (token.isNullOrBlank()) AppResult.Error(AppError.Network.UNAUTHORIZED)
    else request(token)
}
