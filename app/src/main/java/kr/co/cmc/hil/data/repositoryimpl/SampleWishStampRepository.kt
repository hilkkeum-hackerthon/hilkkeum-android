package kr.co.cmc.hil.data.repositoryimpl

import javax.inject.Inject
import kr.co.cmc.hil.domain.repository.WishStampRepository

internal class SampleWishStampRepository @Inject constructor() : WishStampRepository {
    override fun getEarnedStampDates(): List<String> =
        listOf("2026.01.21", "2026.01.24", "2026.01.24")
}
