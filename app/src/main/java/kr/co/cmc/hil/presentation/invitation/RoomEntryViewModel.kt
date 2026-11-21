package kr.co.cmc.hil.presentation.invitation

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.cmc.hil.BuildConfig
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.common.map
import kr.co.cmc.hil.domain.model.room.RoomSession
import kr.co.cmc.hil.domain.usecase.room.CreateRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.DeleteRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.GetRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.JoinRoomUseCase
import kr.co.cmc.hil.domain.usecase.room.RoomSessionUseCase
import kr.co.cmc.hil.presentation.common.MviViewModel

internal data class RoomEntryUiState(
    val isRestoring: Boolean = true,
    val isSubmitting: Boolean = false,
    val isLeaving: Boolean = false,
    val session: RoomSession? = null,
    val isPaired: Boolean = false,
    val statusError: Int? = null,
)

internal sealed interface RoomEntryAction {
    data object Create : RoomEntryAction
    data class Join(val code: String) : RoomEntryAction
    data class ObserveStatus(val observing: Boolean) : RoomEntryAction

    /** 상대가 들어오기 전 방장이 초대 화면에서 뒤로 가면 방을 삭제하고 첫 화면으로 돌아간다. */
    data object Leave : RoomEntryAction
}

private const val RoomStatusPollIntervalMillis = 3_000L

@HiltViewModel
internal class RoomEntryViewModel @Inject constructor(
    private val createRoom: CreateRoomUseCase,
    private val joinRoom: JoinRoomUseCase,
    private val getRoom: GetRoomUseCase,
    private val deleteRoom: DeleteRoomUseCase,
    private val roomSession: RoomSessionUseCase,
) : MviViewModel<RoomEntryUiState, RoomEntryAction, Int>(RoomEntryUiState()) {
    private var statusJob: Job? = null

    init {
        viewModelScope.launch {
            when (val result = roomSession()) {
                is AppResult.Success -> updateState { it.copy(isRestoring = false, session = result.data) }
                is AppResult.Error -> {
                    updateState { it.copy(isRestoring = false) }
                    emitEvent(R.string.room_request_failed)
                }
            }
        }
    }

    override fun onAction(action: RoomEntryAction) {
        when (action) {
            RoomEntryAction.Create -> submit {
                createRoom().map { RoomSession(it.roomId, it.inviteCode) }
            }
            is RoomEntryAction.Join -> submit {
                joinRoom(action.code.trim()).map { RoomSession(it.roomId, null) }
            }
            RoomEntryAction.Leave -> leave()
            is RoomEntryAction.ObserveStatus -> {
                statusJob?.cancel()
                statusJob = if (action.observing) viewModelScope.launch { observeStatus() } else null
            }
        }
    }

    private suspend fun executeLeave(session: RoomSession) {
        val result = deleteRoom(session.roomId)

        when (result) {
            is AppResult.Success -> updateState {
                it.copy(isLeaving = false, session = null, isPaired = false, statusError = null)
            }
            is AppResult.Error -> {
                updateState { it.copy(isLeaving = false) }
                emitEvent(R.string.room_leave_failed)
            }
        }
    }

    private fun leave() {
        val state = uiState.value
        val session = state.session

        if (state.isLeaving || session == null || session.inviteCode == null || state.isPaired) {
            // 나가는 중이거나, 방장이 상대를 기다리는 상태가 아니다.
        } else {
            statusJob?.cancel()
            statusJob = null
            updateState { it.copy(isLeaving = true) }
            viewModelScope.launch { executeLeave(session) }
        }
    }

    private fun submit(request: suspend () -> AppResult<RoomSession, AppError.Network>) {
        val state = uiState.value
        if (state.isRestoring || state.isSubmitting || state.isLeaving || state.session != null) return
        updateState { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            try {
                if (BuildConfig.BASE_URL.isBlank()) {
                    emitEvent(R.string.room_server_missing)
                    return@launch
                }
                when (val result = request()) {
                    is AppResult.Success -> updateState { it.copy(session = result.data) }
                    is AppResult.Error -> emitEvent(
                        when (result.error) {
                            AppError.Network.NOT_FOUND, AppError.Network.BAD_REQUEST ->
                                R.string.invitation_invalid_code
                            AppError.Network.CONFLICT -> R.string.room_full
                            else -> R.string.room_request_failed
                        },
                    )
                }
            } finally {
                updateState { it.copy(isSubmitting = false) }
            }
        }
    }

    private suspend fun observeStatus() {
        val session = uiState.value.session ?: return
        if (BuildConfig.BASE_URL.isBlank()) {
            updateState { it.copy(statusError = R.string.room_server_missing) }
            return
        }
        while (!uiState.value.isPaired) {
            when (val result = getRoom(session.roomId)) {
                is AppResult.Success -> updateState {
                    it.copy(isPaired = result.data.isPartnerJoined, statusError = null)
                }
                is AppResult.Error -> when (result.error) {
                    AppError.Network.UNAUTHORIZED, AppError.Network.FORBIDDEN, AppError.Network.NOT_FOUND -> {
                        if (roomSession.clear() is AppResult.Success) {
                            updateState { it.copy(session = null, statusError = null) }
                            emitEvent(R.string.room_session_expired)
                            return
                        }
                        updateState { it.copy(statusError = R.string.room_status_failed) }
                    }
                    else -> updateState { it.copy(statusError = R.string.room_status_failed) }
                }
            }
            if (!uiState.value.isPaired) delay(RoomStatusPollIntervalMillis)
        }
    }
}
