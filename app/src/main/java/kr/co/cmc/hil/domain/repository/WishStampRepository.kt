package kr.co.cmc.hil.domain.repository

internal interface WishStampRepository {
    fun getEarnedStampDates(): List<String>
}
