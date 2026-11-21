package kr.co.cmc.hil.presentation.room

import kr.co.cmc.hil.domain.model.RoomError

data class RoomUiState(
    val isRestoring: Boolean = true,
    val isSubmitting: Boolean = false,
    val roomId: Long? = null,
    val inviteCode: String? = null,
    val isCreator: Boolean = false,
    val isPaired: Boolean = false,
    val requestError: RoomError? = null,
    val statusError: RoomError? = null,
)
