package kr.co.cmc.hil.data.mapper

import kr.co.cmc.hil.data.dto.stamp.StampsResponseDto
import kr.co.cmc.hil.domain.model.room.MemberId
import kr.co.cmc.hil.domain.model.stamp.Stamps

/**
 * 스탬프 조회 응답을 도메인 모델 [Stamps]로 변환한다.
 */
fun StampsResponseDto.toDomain(): Stamps? {
    val count = stampCount ?: heartCount ?: return null
    val maximum = maxStamps ?: maxHearts ?: return null

    return Stamps(
        memberId = memberId?.let(::MemberId),
        heartCount = count,
        maxHearts = maximum,
        canExchange = canExchange ?: (count >= maximum && !hasActiveCoupon),
        hasActiveCoupon = hasActiveCoupon,
    )
}
