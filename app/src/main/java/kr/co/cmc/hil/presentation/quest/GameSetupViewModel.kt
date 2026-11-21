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
import kr.co.cmc.hil.domain.model.mission.MissionId
import kr.co.cmc.hil.domain.model.mission.PenaltyId
import kr.co.cmc.hil.domain.model.room.RoomSession
import kr.co.cmc.hil.domain.usecase.game.CreateGameUseCase
import kr.co.cmc.hil.domain.usecase.game.EndGameUseCase
import kr.co.cmc.hil.domain.usecase.game.GetActiveGameIdUseCase
import kr.co.cmc.hil.domain.usecase.game.GetGameUseCase
import kr.co.cmc.hil.domain.usecase.game.SelectMissionsUseCase
import kr.co.cmc.hil.domain.usecase.game.SelectPenaltyUseCase
import kr.co.cmc.hil.presentation.common.MviViewModel

/**
 * [gameStatus]는 [gameId] 게임을 마지막으로 조회한 상태다.
 */
internal data class GameSetupUiState(
    val isLoading: Boolean = false,
    val gameId: Long? = null,
    val gameStatus: GameStatus? = null,
    val hasStatusError: Boolean = false,
)

internal sealed interface GameSetupAction {
    data class Continue(val room: RoomSession, val mode: QuestMode, val count: Int) : GameSetupAction
    data class SelectMissions(val gameId: Long, val missionIds: List<Int>) : GameSetupAction
    data class SelectPenalty(val gameId: Long, val missionIds: List<Int>, val penaltyId: Int) : GameSetupAction
    data class End(val gameId: Long) : GameSetupAction
    data class Observe(val gameId: Long?) : GameSetupAction
}

internal sealed interface GameSetupEvent {
    data class OpenSelection(val gameId: Long, val count: Int) : GameSetupEvent
    data class OpenPenalty(val gameId: Long, val missionIds: List<Int>) : GameSetupEvent
    data class OpenWaiting(val gameId: Long, val missionIds: List<Int>, val penaltyId: Int) : GameSetupEvent
    data class ShowMessage(val message: Int) : GameSetupEvent
}

private const val GameStatusPollIntervalMillis = 3_000L

@HiltViewModel
internal class GameSetupViewModel @Inject constructor(
    private val createGame: CreateGameUseCase,
    private val getGame: GetGameUseCase,
    private val getActiveGameId: GetActiveGameIdUseCase,
    private val selectMissions: SelectMissionsUseCase,
    private val selectPenalty: SelectPenaltyUseCase,
    private val endGame: EndGameUseCase,
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

    private suspend fun submitMissions(action: GameSetupAction.SelectMissions) {
        val missionIds = action.missionIds.map { MissionId(it.toLong()) }
        when (selectMissions(GameId(action.gameId), missionIds)) {
            is AppResult.Success -> emitEvent(GameSetupEvent.OpenPenalty(action.gameId, action.missionIds))
            is AppResult.Error -> emitEvent(GameSetupEvent.ShowMessage(R.string.game_select_missions_failed))
        }
    }

    private suspend fun submitPenalty(action: GameSetupAction.SelectPenalty) {
        when (selectPenalty(GameId(action.gameId), PenaltyId(action.penaltyId.toLong()))) {
            is AppResult.Success -> emitEvent(
                GameSetupEvent.OpenWaiting(action.gameId, action.missionIds, action.penaltyId),
            )
            is AppResult.Error -> emitEvent(GameSetupEvent.ShowMessage(R.string.game_select_penalty_failed))
        }
    }

    private suspend fun end(gameId: Long) {
        when (endGame(GameId(gameId))) {
            is AppResult.Success -> refreshStatus(gameId)
            is AppResult.Error -> emitEvent(GameSetupEvent.ShowMessage(R.string.game_end_failed))
        }
    }

    private suspend fun refreshStatus(gameId: Long) {
        when (val result = getGame(GameId(gameId))) {
            is AppResult.Success -> updateState {
                it.copy(gameId = gameId, gameStatus = result.data.status, hasStatusError = false)
            }
            is AppResult.Error -> updateState { it.copy(hasStatusError = true) }
        }
    }

    private suspend fun pollStatus(gameId: Long) {
        while (true) {
            refreshStatus(gameId)
            delay(GameStatusPollIntervalMillis)
        }
    }

    private fun launchLoading(block: suspend () -> Unit) {
        if (uiState.value.isLoading) return
        updateState { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                block()
            } finally {
                updateState { it.copy(isLoading = false) }
            }
        }
    }

    override fun onAction(action: GameSetupAction) {
        when (action) {
            is GameSetupAction.Continue -> launchLoading { continueSetup(action) }
            is GameSetupAction.SelectMissions -> launchLoading { submitMissions(action) }
            is GameSetupAction.SelectPenalty -> launchLoading { submitPenalty(action) }
            is GameSetupAction.End -> launchLoading { end(action.gameId) }
            is GameSetupAction.Observe -> {
                statusJob?.cancel()
                statusJob = action.gameId?.let { id -> viewModelScope.launch { pollStatus(id) } }
            }
        }
    }
}
