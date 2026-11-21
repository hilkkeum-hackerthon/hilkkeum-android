package kr.co.cmc.hil.data.mapper

import kr.co.cmc.hil.data.dto.mission.MissionDto
import kr.co.cmc.hil.data.dto.mission.MissionsResponseDto
import kr.co.cmc.hil.data.dto.mission.PenaltiesResponseDto
import kr.co.cmc.hil.data.dto.mission.PenaltyDto
import kr.co.cmc.hil.domain.model.mission.Mission
import kr.co.cmc.hil.domain.model.mission.MissionId
import kr.co.cmc.hil.domain.model.mission.Penalty
import kr.co.cmc.hil.domain.model.mission.PenaltyId

/**
 * 미션 목록 조회 응답을 도메인 [Mission] 목록으로 변환한다.
 */
fun MissionsResponseDto.toDomain(): List<Mission> = missions.map { it.toDomain() }

/**
 * 벌칙 목록 조회 응답을 도메인 [Penalty] 목록으로 변환한다.
 */
fun PenaltiesResponseDto.toDomain(): List<Penalty> = penalties.map { it.toDomain() }

/**
 * 미션 한 건을 도메인 모델 [Mission]으로 변환한다.
 */
private fun MissionDto.toDomain(): Mission = Mission(
    id = MissionId(id),
    content = content,
)

/**
 * 벌칙 한 건을 도메인 모델 [Penalty]로 변환한다.
 */
private fun PenaltyDto.toDomain(): Penalty = Penalty(
    id = PenaltyId(id),
    content = content,
)
