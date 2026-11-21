package kr.co.cmc.hil.data.repositoryimpl

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kr.co.cmc.hil.data.dto.mission.MissionsResponseDto
import kr.co.cmc.hil.data.dto.mission.PenaltiesResponseDto
import kr.co.cmc.hil.data.mapper.toDomain
import kr.co.cmc.hil.data.mapper.toDto
import kr.co.cmc.hil.data.safeApiCall
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.common.map
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.mission.Mission
import kr.co.cmc.hil.domain.model.mission.Penalty
import kr.co.cmc.hil.domain.repository.MissionRepository
import javax.inject.Inject

class MissionRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
) : MissionRepository {

    /**
     * `GET api/v1/missions?dateType=`를 호출하고 응답을 [Mission] 목록으로 변환한다.
     */
    override suspend fun getMissions(dateType: DateType): AppResult<List<Mission>, AppError.Network> {
        return safeApiCall<MissionsResponseDto> {
            httpClient.get("api/v1/missions") {
                parameter("dateType", dateType.toDto().name)
            }
        }.map { it.toDomain() }
    }

    /**
     * `GET api/v1/penalties`를 호출하고 응답을 [Penalty] 목록으로 변환한다.
     */
    override suspend fun getPenalties(): AppResult<List<Penalty>, AppError.Network> {
        return safeApiCall<PenaltiesResponseDto> {
            httpClient.get("api/v1/penalties")
        }.map { it.toDomain() }
    }
}
