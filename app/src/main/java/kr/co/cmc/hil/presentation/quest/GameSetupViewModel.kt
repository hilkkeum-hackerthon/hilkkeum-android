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
import kr.co.cmc.hil.domain.model.game.GuessId
import kr.co.cmc.hil.domain.model.room.RoomSession
import kr.co.cmc.hil.domain.usecase.game.CreateGameUseCase
import kr.co.cmc.hil.domain.usecase.game.GetActiveGameIdUseCase
import kr.co.cmc.hil.domain.usecase.game.GetGameUseCase
import kr.co.cmc.hil.domain.model.mission.Mission
import kr.co.cmc.hil.domain.model.mission.Penalty
import kr.co.cmc.hil.domain.model.game.GameResult
import kr.co.cmc.hil.domain.model.game.ReceivedGuesses
import kr.co.cmc.hil.domain.usecase.game.EndGameUseCase
import kr.co.cmc.hil.domain.usecase.game.GetGameResultUseCase
import kr.co.cmc.hil.domain.usecase.game.GetReceivedGuessesUseCase
import kr.co.cmc.hil.domain.usecase.game.JudgeGuessUseCase
import kr.co.cmc.hil.domain.usecase.game.SubmitGuessesUseCase
import kr.co.cmc.hil.domain.usecase.game.SelectMissionsUseCase
import kr.co.cmc.hil.domain.usecase.game.SelectPenaltyUseCase
import kr.co.cmc.hil.domain.usecase.mission.GetMissionsUseCase
import kr.co.cmc.hil.domain.usecase.mission.GetPenaltiesUseCase
import kr.co.cmc.hil.presentation.common.MviViewModel

internal data class GameSetupUiState(
    val isLoading: Boolean = false,
    val playingGameId: Long? = null,
    val hasStatusError: Boolean = false,
    val missions: List<Mission>? = null,
    val penalties: List<Penalty>? = null,
    val hasCatalogError: Boolean = false,
    val guessingGameId: Long? = null,
    val finishedGameId: Long? = null,
    val receivedGuesses: ReceivedGuesses? = null,
    val result: GameResult? = null,
    val hasResultError: Boolean = false,
    val judgedGameId: Long? = null,
)

internal sealed interface GameSetupAction {
    data class Continue(val room: RoomSession, val mode: QuestMode, val count: Int) : GameSetupAction
    data class LoadMissions(val dateType: DateType) : GameSetupAction
    data object LoadPenalties : GameSetupAction
    data class SubmitMissions(val gameId: Long, val indices: List<Int>) : GameSetupAction
    data class SubmitPenalty(val gameId: Long, val index: Int) : GameSetupAction
    data class End(val gameId: Long) : GameSetupAction
    data class SubmitGuesses(val gameId: Long, val answers: List<String>) : GameSetupAction
    data class Judge(val gameId: Long, val correctIndices: List<Int>) : GameSetupAction
    data class LoadResult(val gameId: Long) : GameSetupAction
    data class Observe(val gameId: Long?, val fetchGuesses: Boolean = false) : GameSetupAction
}

internal sealed interface GameSetupEvent {
    data class OpenSelection(val gameId: Long, val count: Int, val dateType: DateType) : GameSetupEvent
    data class MissionsSubmitted(val labels: List<String>) : GameSetupEvent
    data class PenaltySubmitted(val label: String) : GameSetupEvent
    data object GuessesSubmitted : GameSetupEvent
    data class ShowMessage(val message: Int) : GameSetupEvent
}

private const val GameStatusPollIntervalMillis = 3_000L
private const val PlayingStatus = "PLAYING"

@HiltViewModel
internal class GameSetupViewModel @Inject constructor(
    private val createGame: CreateGameUseCase,
    private val getGame: GetGameUseCase,
    private val getActiveGameId: GetActiveGameIdUseCase,
    private val getMissions: GetMissionsUseCase,
    private val getPenalties: GetPenaltiesUseCase,
    private val selectMissions: SelectMissionsUseCase,
    private val selectPenalty: SelectPenaltyUseCase,
    private val endGame: EndGameUseCase,
    private val submitGuesses: SubmitGuessesUseCase,
    private val getReceivedGuesses: GetReceivedGuessesUseCase,
    private val judgeGuess: JudgeGuessUseCase,
    private val getGameResult: GetGameResultUseCase,
) : MviViewModel<GameSetupUiState, GameSetupAction, GameSetupEvent>(GameSetupUiState()) {
    private var statusJob: Job? = null
    private var catalogJob: Job? = null
    private val judgedGuessIds = mutableSetOf<GuessId>()

    private suspend fun continueSetup(action: GameSetupAction.Continue) {
        val (room, mode, count) = action
        if (room.inviteCode != null) {
            val dateType = if (mode == QuestMode.Together) DateType.OFFLINE else DateType.ONLINE
            when (val result = createGame(room.roomId, dateType, count)) {
                is AppResult.Success -> emitEvent(
                    GameSetupEvent.OpenSelection(result.data.gameId.value, result.data.missionCount, result.data.dateType),
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
                    emitEvent(GameSetupEvent.OpenSelection(gameId.value, game.missionCount, game.dateType))
                }
                is AppResult.Error -> emitEvent(GameSetupEvent.ShowMessage(R.string.game_status_failed))
            }
        }
    }

    private suspend fun pollStatus(gameId: Long, fetchGuesses: Boolean) {
        while (true) {
            when (val result = getGame(GameId(gameId))) {
                is AppResult.Success -> {
                    val status = result.data.status
                    updateState {
                        it.copy(
                            playingGameId = gameId.takeIf { status == PlayingStatus },
                            guessingGameId = gameId.takeIf { status == "GUESSING" },
                            finishedGameId = gameId.takeIf { status == "FINISHED" },
                            hasStatusError = false,
                        )
                    }
                    if (status == "FINISHED") return
                    if (fetchGuesses && status == "GUESSING") {
                        when (val guesses = getReceivedGuesses(GameId(gameId))) {
                            is AppResult.Success -> updateState { it.copy(receivedGuesses = guesses.data) }
                            is AppResult.Error -> updateState { it.copy(hasStatusError = true) }
                        }
                    }
                }
                is AppResult.Error -> updateState { it.copy(hasStatusError = true) }
            }
            delay(GameStatusPollIntervalMillis)
        }
    }

    private fun loadCatalog(request: suspend () -> Unit) {
        catalogJob?.cancel()
        catalogJob = viewModelScope.launch { request() }
    }

    private fun runRequest(request: suspend () -> Unit) {
        if (uiState.value.isLoading) return
        updateState { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                request()
            } finally {
                updateState { it.copy(isLoading = false) }
            }
        }
    }

    override fun onAction(action: GameSetupAction) {
        when (action) {
            is GameSetupAction.Continue -> {
                runRequest { continueSetup(action) }
            }
            is GameSetupAction.LoadMissions -> loadCatalog {
                updateState { it.copy(missions = null, hasCatalogError = false) }
                when (val result = getMissions(action.dateType)) {
                    is AppResult.Success -> updateState { it.copy(missions = result.data) }
                    is AppResult.Error -> updateState { it.copy(hasCatalogError = true) }
                }
            }
            GameSetupAction.LoadPenalties -> loadCatalog {
                updateState { it.copy(penalties = null, hasCatalogError = false) }
                when (val result = getPenalties()) {
                    is AppResult.Success -> updateState { it.copy(penalties = result.data) }
                    is AppResult.Error -> updateState { it.copy(hasCatalogError = true) }
                }
            }
            is GameSetupAction.SubmitMissions -> runRequest {
                val catalog = uiState.value.missions.orEmpty()
                val selections = action.indices.mapNotNull(catalog::getOrNull)
                if (selections.isNotEmpty() && selections.size == action.indices.size) {
                    when (selectMissions(GameId(action.gameId), selections.map { it.id })) {
                        is AppResult.Success -> emitEvent(GameSetupEvent.MissionsSubmitted(selections.map { it.content }))
                        is AppResult.Error -> emitEvent(GameSetupEvent.ShowMessage(R.string.game_selection_failed))
                    }
                }
            }
            is GameSetupAction.SubmitPenalty -> runRequest {
                val penalty = uiState.value.penalties.orEmpty().getOrNull(action.index)
                if (penalty != null) {
                    when (selectPenalty(GameId(action.gameId), penalty.id)) {
                        is AppResult.Success -> emitEvent(GameSetupEvent.PenaltySubmitted(penalty.content))
                        is AppResult.Error -> emitEvent(GameSetupEvent.ShowMessage(R.string.game_selection_failed))
                    }
                }
            }
            is GameSetupAction.End -> runRequest {
                when (endGame(GameId(action.gameId))) {
                    is AppResult.Success -> updateState { it.copy(guessingGameId = action.gameId) }
                    is AppResult.Error -> emitEvent(GameSetupEvent.ShowMessage(R.string.game_selection_failed))
                }
            }
            is GameSetupAction.SubmitGuesses -> runRequest {
                when (submitGuesses(GameId(action.gameId), action.answers)) {
                    is AppResult.Success -> emitEvent(GameSetupEvent.GuessesSubmitted)
                    is AppResult.Error -> emitEvent(GameSetupEvent.ShowMessage(R.string.game_selection_failed))
                }
            }
            is GameSetupAction.Judge -> runRequest {
                val received = uiState.value.receivedGuesses?.takeIf { it.gameId.value == action.gameId }
                if (received != null) {
                    // A correct guess finishes the round immediately on the server.
                    val correct = action.correctIndices.toSet()
                    val ordered = received.guesses.withIndex().sortedBy { it.index in correct }
                    for ((index, guess) in ordered) {
                        if (guess.guessId in judgedGuessIds) continue
                        val isCorrect = index in correct
                        when (judgeGuess(GameId(action.gameId), guess.guessId, isCorrect)) {
                            is AppResult.Success -> {
                                judgedGuessIds += guess.guessId
                                if (isCorrect) {
                                    updateState { it.copy(finishedGameId = action.gameId) }
                                    break
                                }
                            }
                            is AppResult.Error -> {
                                emitEvent(GameSetupEvent.ShowMessage(R.string.game_selection_failed))
                                return@runRequest
                            }
                        }
                    }
                    updateState { it.copy(judgedGameId = action.gameId) }
                }
            }
            is GameSetupAction.LoadResult -> viewModelScope.launch {
                updateState { it.copy(hasResultError = false) }
                when (val result = getGameResult(GameId(action.gameId))) {
                    is AppResult.Success -> updateState { it.copy(result = result.data) }
                    is AppResult.Error -> updateState { it.copy(hasResultError = true) }
                }
            }
            is GameSetupAction.Observe -> {
                statusJob?.cancel()
                statusJob = action.gameId?.let { id -> viewModelScope.launch { pollStatus(id, action.fetchGuesses) } }
            }
        }
    }
}
