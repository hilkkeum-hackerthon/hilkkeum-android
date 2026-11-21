package kr.co.cmc.hil.presentation.room

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kr.co.cmc.hil.domain.common.AppError
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.RoomSession
import kr.co.cmc.hil.domain.usecase.ManageRoomUseCase

private const val RoomPollIntervalMillis = 3_000L

@HiltViewModel
class RoomViewModel @Inject constructor(private val rooms: ManageRoomUseCase) : ViewModel() {
    private val state = MutableStateFlow(RoomUiState())

    val uiState = state.asStateFlow()

    private var session: RoomSession? = null

    private var polling: Job? = null

    private fun publishSession(value: RoomSession?) {
        session = value
        state.update {
            it.copy(roomId = value?.roomId, inviteCode = value?.inviteCode, isCreator = value?.isCreator == true)
        }
    }

    private fun submit(request: suspend () -> AppResult<RoomSession>) {
        if (!state.value.isSubmitting && session == null) {
            state.update { it.copy(isSubmitting = true, requestError = null) }
            viewModelScope.launch {
                when (val result = request()) {
                    is AppResult.Success -> {
                        val persistence = rooms.save(result.value)
                        publishSession(result.value)
                        state.update { it.copy(statusError = (persistence as? AppResult.Failure)?.error) }
                    }
                    is AppResult.Failure -> state.update { it.copy(requestError = result.error) }
                }
                state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    private fun startPolling() {
        val current = session
        if (current != null && polling?.isActive != true && !state.value.isPaired) {
            polling = viewModelScope.launch {
                while (isActive) {
                    when (val result = rooms.status(current)) {
                        is AppResult.Success -> {
                            state.update {
                                it.copy(
                                    isPaired = result.value.isPaired,
                                    statusError = it.statusError.takeIf { error -> error == AppError.StorageError },
                                )
                            }
                            if (result.value.isPaired) break
                        }
                        is AppResult.Failure -> {
                            if (result.error in setOf(AppError.NotFound, AppError.Unauthorized, AppError.PermissionDenied)) {
                                rooms.clear()
                                publishSession(null)
                                state.update { it.copy(requestError = result.error, statusError = null) }
                                break
                            }
                            state.update { it.copy(statusError = result.error) }
                        }
                    }
                    delay(RoomPollIntervalMillis)
                }
            }
        }
    }

    fun onAction(action: RoomAction) {
        when (action) {
            RoomAction.Create -> submit { rooms.create() }
            is RoomAction.Join -> submit { rooms.join(action.inviteCode) }
            RoomAction.StartPolling -> startPolling()
            RoomAction.StopPolling -> {
                polling?.cancel()
                polling = null
            }
            RoomAction.DismissRequestError -> state.update { it.copy(requestError = null) }
        }
    }

    init {
        viewModelScope.launch {
            when (val restored = rooms.restore()) {
                is AppResult.Success -> publishSession(restored.value)
                is AppResult.Failure -> {
                    rooms.clear()
                    state.update { it.copy(requestError = restored.error) }
                }
            }
            state.update { it.copy(isRestoring = false) }
        }
    }
}
