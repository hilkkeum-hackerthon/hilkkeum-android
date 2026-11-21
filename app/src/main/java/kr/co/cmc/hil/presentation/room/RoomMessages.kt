package kr.co.cmc.hil.presentation.room

import androidx.annotation.StringRes
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.model.RoomError

@StringRes
fun RoomError.roomMessage(isJoining: Boolean = false): Int = when (this) {
    RoomError.MissingServer -> R.string.room_server_missing
    RoomError.ValidationFailed -> if (isJoining) R.string.room_invalid_code else R.string.room_request_failed
    RoomError.NotFound -> if (isJoining) R.string.room_code_not_found else R.string.room_session_unavailable
    RoomError.RoomFull -> R.string.room_full
    RoomError.Unauthorized, RoomError.PermissionDenied -> R.string.room_session_unavailable
    RoomError.NetworkError -> R.string.room_network_error
    RoomError.ServerError, RoomError.InvalidResponse -> R.string.room_request_failed
    RoomError.StorageError -> R.string.room_storage_error
}
