package kr.co.cmc.hil.presentation.room

import kr.co.cmc.hil.domain.common.AppError

data class RoomUiState(
    val isRestoring: Boolean = true,
    val isSubmitting: Boolean = false,
    val roomId: Long? = null,
    val inviteCode: String? = null,
    val isCreator: Boolean = false,
    val isPaired: Boolean = false,
    val requestError: AppError? = null,
    val statusError: AppError? = null,
)
