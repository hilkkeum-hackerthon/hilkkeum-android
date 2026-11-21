package kr.co.cmc.hil.data.dto.mission

import kotlinx.serialization.Serializable

/**
 * `GET /api/v1/missions` 응답.
 */
@Serializable
data class MissionsResponseDto(
    val missions: List<MissionDto>,
)

@Serializable
data class MissionDto(
    val id: Long,
    val content: String,
)
