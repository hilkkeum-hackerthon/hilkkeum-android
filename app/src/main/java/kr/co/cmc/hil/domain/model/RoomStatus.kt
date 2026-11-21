package kr.co.cmc.hil.domain.model

data class RoomStatus(val roomId: Long, val status: String, val memberCount: Int) {
    val isPaired: Boolean get() = status == "ACTIVE" && memberCount == 2
}
