package kr.co.cmc.hil.presentation.room

import androidx.annotation.StringRes
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.common.AppError

@StringRes
fun AppError.roomMessage(isJoining: Boolean = false): Int = when (this) {
    AppError.MissingServer -> R.string.room_server_missing
    AppError.ValidationFailed -> if (isJoining) R.string.room_invalid_code else R.string.room_request_failed
    AppError.NotFound -> if (isJoining) R.string.room_code_not_found else R.string.room_session_unavailable
    AppError.RoomFull -> R.string.room_full
    AppError.Unauthorized, AppError.PermissionDenied -> R.string.room_session_unavailable
    AppError.NetworkError -> R.string.room_network_error
    AppError.ServerError, AppError.InvalidResponse -> R.string.room_request_failed
    AppError.StorageError -> R.string.room_storage_error
}
