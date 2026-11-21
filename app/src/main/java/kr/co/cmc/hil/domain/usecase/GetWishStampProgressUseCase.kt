package kr.co.cmc.hil.domain.usecase

import kr.co.cmc.hil.domain.model.WishStampProgress
import kr.co.cmc.hil.domain.repository.WishStampRepository

internal class GetWishStampProgressUseCase(private val repository: WishStampRepository) {
    operator fun invoke(usedWishes: Int): WishStampProgress =
        WishStampProgress(earnedDates = repository.getEarnedStampDates(), usedWishes = usedWishes)
}
