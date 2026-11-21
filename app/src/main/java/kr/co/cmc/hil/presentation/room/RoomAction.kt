package kr.co.cmc.hil.presentation.room

sealed interface RoomAction {
    data object Create : RoomAction
    data class Join(val inviteCode: String) : RoomAction
    data object StartPolling : RoomAction
    data object StopPolling : RoomAction
    data object DismissRequestError : RoomAction
}
