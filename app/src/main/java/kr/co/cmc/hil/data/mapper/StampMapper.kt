package kr.co.cmc.hil.data.mapper

import kr.co.cmc.hil.data.dto.stamp.StampsResponseDto
import kr.co.cmc.hil.domain.model.room.MemberId
import kr.co.cmc.hil.domain.model.stamp.Stamps

/**
 * 스탬프 조회 응답을 도메인 모델 [Stamps]로 변환한다.
 */
fun StampsResponseDto.toDomain(): Stamps = Stamps(
    memberId = MemberId(memberId),
    heartCount = heartCount,
    maxHearts = maxHearts,
    canExchange = canExchange,
    hasActiveCoupon = hasActiveCoupon,
)
