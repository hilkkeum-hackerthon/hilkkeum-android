package kr.co.cmc.hil.data.repositoryimpl

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kr.co.cmc.hil.data.dto.stamp.StampsResponseDto
import kr.co.cmc.hil.data.mapper.toDomain
import kr.co.cmc.hil.data.safeApiCall
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.common.map
import kr.co.cmc.hil.domain.model.stamp.Stamps
import kr.co.cmc.hil.domain.repository.StampRepository
import javax.inject.Inject

class StampRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
) : StampRepository {

    /**
     * `GET api/v1/stamps`를 호출하고 응답을 [Stamps]로 변환한다.
     */
    override suspend fun getStamps(): AppResult<Stamps, AppError.Network> {
        return safeApiCall<StampsResponseDto> {
            httpClient.get("api/v1/stamps")
        }.map { it.toDomain() }
    }
}
