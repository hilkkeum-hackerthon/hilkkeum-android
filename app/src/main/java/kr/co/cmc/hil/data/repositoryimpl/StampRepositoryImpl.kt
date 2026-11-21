package kr.co.cmc.hil.data.repositoryimpl

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import kr.co.cmc.hil.data.datastore.TokenStorage
import kr.co.cmc.hil.data.datastore.withMemberToken
import kr.co.cmc.hil.data.dto.stamp.StampsResponseDto
import kr.co.cmc.hil.data.mapper.toDomain
import kr.co.cmc.hil.data.safeApiCall
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.stamp.Stamps
import kr.co.cmc.hil.domain.repository.StampRepository
import javax.inject.Inject

class StampRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val tokenStorage: TokenStorage,
) : StampRepository {

    /**
     * `GET api/v1/stamps`를 호출하고 응답을 [Stamps]로 변환한다.
     */
    override suspend fun getStamps(): AppResult<Stamps, AppError.Network> {
        return tokenStorage.withMemberToken { token ->
            when (val result = safeApiCall<StampsResponseDto> {
                httpClient.get("api/v1/stamps") { bearerAuth(token) }
            }) {
                is AppResult.Success -> {
                    val stamps = result.data.toDomain()
                    if (stamps != null) AppResult.Success(stamps)
                    else AppResult.Error(AppError.Network.SERIALIZATION)
                }
                is AppResult.Error -> result
            }
        }
    }
}
