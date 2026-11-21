package kr.co.cmc.hil.presentation.quest

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.common.AppResult
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.domain.model.game.GameId
import kr.co.cmc.hil.domain.model.game.GameStatus
import kr.co.cmc.hil.domain.model.room.RoomSession
import kr.co.cmc.hil.domain.usecase.game.CreateGameUseCase
import kr.co.cmc.hil.domain.usecase.game.GetActiveGameIdUseCase
import kr.co.cmc.hil.domain.usecase.game.GetGameUseCase
import kr.co.cmc.hil.presentation.common.MviViewModel

internal data class GameSetupUiState(
    val isLoading: Boolean = false,
    val playingGameId: Long? = null,
    val hasStatusError: Boolean = false,
)

internal sealed interface GameSetupAction {
    data class Continue(val room: RoomSession, val mode: QuestMode, val count: Int) : GameSetupAction
    data class Observe(val gameId: Long?) : GameSetupAction
}

internal sealed interface GameSetupEvent {
    data class OpenSelection(val gameId: Long, val count: Int) : GameSetupEvent
    data class ShowMessage(val message: Int) : GameSetupEvent
}

private const val GameStatusPollIntervalMillis = 3_000L

@HiltViewModel
internal class GameSetupViewModel @Inject constructor(
    private val createGame: CreateGameUseCase,
    private val getGame: GetGameUseCase,
    private val getActiveGameId: GetActiveGameIdUseCase,
) : MviViewModel<GameSetupUiState, GameSetupAction, GameSetupEvent>(GameSetupUiState()) {
    private var statusJob: Job? = null

    private suspend fun continueSetup(action: GameSetupAction.Continue) {
        val (room, mode, count) = action
        if (room.inviteCode != null) {
            val dateType = if (mode == QuestMode.Together) DateType.OFFLINE else DateType.ONLINE
            when (val result = createGame(room.roomId, dateType, count)) {
                is AppResult.Success -> emitEvent(
                    GameSetupEvent.OpenSelection(result.data.gameId.value, result.data.missionCount),
                )
                is AppResult.Error -> emitEvent(GameSetupEvent.ShowMessage(R.string.game_create_failed))
            }
        } else {
            val activeGameResult = getActiveGameId(room.roomId)
            val gameId = when (activeGameResult) {
                is AppResult.Success -> activeGameResult.data
                is AppResult.Error -> {
                    emitEvent(GameSetupEvent.ShowMessage(R.string.game_status_failed))
                    return
                }
            }
            if (gameId == null) {
                emitEvent(GameSetupEvent.ShowMessage(R.string.game_wait_for_creator))
                return
            }
            when (val result = getGame(gameId)) {
                is AppResult.Success -> {
                    val game = result.data
                    emitEvent(GameSetupEvent.OpenSelection(gameId.value, game.missionCount))
                }
                is AppResult.Error -> emitEvent(GameSetupEvent.ShowMessage(R.string.game_status_failed))
            }
        }
    }

    private suspend fun pollStatus(gameId: Long) {
        while (uiState.value.playingGameId != gameId) {
            when (val result = getGame(GameId(gameId))) {
                is AppResult.Success -> updateState {
                    it.copy(
                        playingGameId = gameId.takeIf { result.data.status == GameStatus.PLAYING },
                        hasStatusError = false,
                    )
                }
                is AppResult.Error -> updateState { it.copy(hasStatusError = true) }
            }
            if (uiState.value.playingGameId != gameId) delay(GameStatusPollIntervalMillis)
        }
    }

    override fun onAction(action: GameSetupAction) {
        when (action) {
            is GameSetupAction.Continue -> {
                if (uiState.value.isLoading) return
                updateState { it.copy(isLoading = true) }
                viewModelScope.launch {
                    try {
                        continueSetup(action)
                    } finally {
                        updateState { it.copy(isLoading = false) }
                    }
                }
            }
            is GameSetupAction.Observe -> {
                statusJob?.cancel()
                statusJob = action.gameId?.let { id -> viewModelScope.launch { pollStatus(id) } }
            }
        }
    }
}
