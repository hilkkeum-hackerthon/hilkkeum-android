package kr.co.cmc.hil.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kr.co.cmc.hil.BuildConfig
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.model.game.GameStatus
import kr.co.cmc.hil.presentation.entrance.EntranceScreen
import kr.co.cmc.hil.presentation.home.TemporaryHomeScreen
import kr.co.cmc.hil.presentation.invitation.InvitationScreen
import kr.co.cmc.hil.presentation.invitation.RoomEntryAction
import kr.co.cmc.hil.presentation.invitation.RoomEntryViewModel
import kr.co.cmc.hil.presentation.quest.GameSetupAction
import kr.co.cmc.hil.presentation.quest.GameSetupEvent
import kr.co.cmc.hil.presentation.quest.GameSetupViewModel
import kr.co.cmc.hil.presentation.quest.QuestCountScreen
import kr.co.cmc.hil.presentation.quest.QuestMode
import kr.co.cmc.hil.presentation.quest.QuestModeScreen
import kr.co.cmc.hil.presentation.quest.QuestPenaltyScreen
import kr.co.cmc.hil.presentation.quest.QuestSelectionScreen
import kr.co.cmc.hil.presentation.quest.operation.MissionGuessScreen
import kr.co.cmc.hil.presentation.quest.operation.MissionJudgementScreen
import kr.co.cmc.hil.presentation.quest.operation.OperationProgressScreen
import kr.co.cmc.hil.presentation.quest.operation.OperationResultScreen
import kr.co.cmc.hil.presentation.quest.operation.OperationWaitingScreen
import kr.co.cmc.hil.presentation.stamp.WishCouponScreen
import kr.co.cmc.hil.presentation.stamp.WishCouponViewModel
import kotlinx.serialization.Serializable

@Serializable
private object EntranceRoute

@Serializable
private object EnterInvitationRoute

@Serializable
private object ShareInvitationRoute

@Serializable
private object TemporaryHomeRoute

@Serializable
private object WishCouponRoute

@Serializable
internal object QuestModeRoute

@Serializable
private data class QuestCountRoute(val mode: QuestMode)

@Serializable
private data class QuestSelectionRoute(val gameId: Long, val missionCount: Int)

@Serializable
private data class QuestPenaltyRoute(val gameId: Long, val missionIds: List<Int>)

@Serializable
private data class OperationWaitingRoute(val gameId: Long, val missionIds: List<Int>, val penaltyId: Int)

@Serializable
private data class OperationProgressRoute(val gameId: Long, val missionIds: List<Int>, val penaltyId: Int)

@Serializable
private data class MissionGuessRoute(val gameId: Long, val missionIds: List<Int>)

@Serializable
private data class MissionJudgementRoute(val gameId: Long, val missionIds: List<Int>)

@Serializable
private data class OperationResultRoute(val partnerCorrectCount: Int)

private const val PreviewGameId = 0L
private const val PreviewPartnerWaitMillis = 1_500L

private const val SubmittedMissionGuessesKey = "submittedMissionGuesses"
private const val MissionJudgementsKey = "missionJudgements"

@Composable
internal fun HilkkeumNavHost(
    wishCouponViewModel: WishCouponViewModel,
    roomEntryViewModel: RoomEntryViewModel,
    gameSetupViewModel: GameSetupViewModel,
    onClose: () -> Unit,
) {
    val isHomePreview = BuildConfig.DEBUG && BuildConfig.BUILD_TYPE == "homePreview"
    val room by roomEntryViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(roomEntryViewModel, context) {
        roomEntryViewModel.event.collect { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
    }
    if (!isHomePreview && room.isRestoring) {
        RoomStatusScreen()
        return
    }
    val navigation = rememberNavController()
    val game by gameSetupViewModel.uiState.collectAsStateWithLifecycle()
    val currentEntry by navigation.currentBackStackEntryAsState()
    val observedGameId = if (isHomePreview) null else currentEntry?.observedGameId()
    LifecycleStartEffect(observedGameId) {
        gameSetupViewModel.onAction(GameSetupAction.Observe(observedGameId))
        onStopOrDispose { gameSetupViewModel.onAction(GameSetupAction.Observe(null)) }
    }
    LaunchedEffect(navigation, gameSetupViewModel, context) {
        gameSetupViewModel.event.collect { event ->
            when (event) {
                is GameSetupEvent.OpenSelection -> {
                    if (navigation.currentDestination?.hasRoute<QuestCountRoute>() == true) {
                        navigation.navigate(QuestSelectionRoute(event.gameId, event.count)) {
                            popUpTo<QuestModeRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                is GameSetupEvent.OpenPenalty -> {
                    if (navigation.currentDestination?.hasRoute<QuestSelectionRoute>() == true) {
                        navigation.navigate(QuestPenaltyRoute(event.gameId, event.missionIds)) {
                            launchSingleTop = true
                        }
                    }
                }
                is GameSetupEvent.OpenWaiting -> {
                    if (navigation.currentDestination?.hasRoute<QuestPenaltyRoute>() == true) {
                        navigation.navigate(OperationWaitingRoute(event.gameId, event.missionIds, event.penaltyId)) {
                            launchSingleTop = true
                        }
                    }
                }
                is GameSetupEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    LaunchedEffect(room.session, room.isPaired) {
        if (isHomePreview) return@LaunchedEffect
        val destination = navigation.currentDestination
        if (room.session == null) {
            if (destination?.hasRoute<EntranceRoute>() == false &&
                !destination.hasRoute<EnterInvitationRoute>()
            ) {
                navigation.navigate(EntranceRoute) {
                    popUpTo(navigation.graph.id)
                    launchSingleTop = true
                }
            }
        } else if (room.isPaired) {
            if (destination?.hasRoute<ShareInvitationRoute>() == true ||
                destination?.hasRoute<EntranceRoute>() == true
            ) {
                navigation.navigate(TemporaryHomeRoute) { popUpTo(navigation.graph.id) }
            }
        } else if (destination?.hasRoute<ShareInvitationRoute>() != true) {
            navigation.navigate(ShareInvitationRoute) { popUpTo(navigation.graph.id) }
        }
    }
    val startDestination: Any = if (isHomePreview) TemporaryHomeRoute else EntranceRoute
    NavHost(navigation, startDestination = startDestination) {
        composable<TemporaryHomeRoute> {
            TemporaryHomeScreen(
                onStartMission = { navigation.navigate(QuestModeRoute) { launchSingleTop = true } },
                onStamp = { navigation.navigate(WishCouponRoute) { launchSingleTop = true } },
            )
        }
        composable<WishCouponRoute> {
            WishCouponScreen(
                viewModel = wishCouponViewModel,
                onHome = { navigation.popBackStack() },
            )
        }
        composable<EntranceRoute> {
            EntranceScreen(
                onEnterInviteCode = { navigation.navigate(EnterInvitationRoute) },
                onCreateRoom = { roomEntryViewModel.onAction(RoomEntryAction.Create) },
            )
        }
        composable<EnterInvitationRoute> {
            InvitationScreen(
                onBack = { navigation.popBackStack() },
                isSubmitting = room.isSubmitting,
                onJoin = { roomEntryViewModel.onAction(RoomEntryAction.Join(it)) },
            )
        }
        composable<ShareInvitationRoute> {
            val session = room.session
            if (session != null) {
                LifecycleStartEffect(session.roomId) {
                    roomEntryViewModel.onAction(RoomEntryAction.ObserveStatus(true))
                    onStopOrDispose {
                        roomEntryViewModel.onAction(RoomEntryAction.ObserveStatus(false))
                    }
                }
                val message = stringResource(room.statusError ?: R.string.room_waiting_for_partner)
                if (session.inviteCode != null) {
                    InvitationScreen(
                        onBack = onClose,
                        shareCode = session.inviteCode,
                        waitingMessage = message,
                    )
                } else {
                    RoomStatusScreen(message)
                }
            }
        }
        composable<QuestModeRoute> {
            QuestModeScreen(onNext = { mode ->
                navigation.navigate(QuestCountRoute(mode)) { launchSingleTop = true }
            })
        }
        composable<QuestCountRoute> { entry ->
            val mode = entry.toRoute<QuestCountRoute>().mode
            QuestCountScreen(isSubmitting = game.isLoading, onNext = { count ->
                if (isHomePreview) {
                    navigation.navigate(QuestSelectionRoute(PreviewGameId, count)) {
                        popUpTo<QuestModeRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    room.session?.let { gameSetupViewModel.onAction(GameSetupAction.Continue(it, mode, count)) }
                }
            })
        }
        composable<QuestSelectionRoute> { entry ->
            val route = entry.toRoute<QuestSelectionRoute>()
            QuestSelectionScreen(missionCount = route.missionCount, onNext = { missionIds ->
                if (isHomePreview) {
                    navigation.navigate(QuestPenaltyRoute(route.gameId, missionIds)) { launchSingleTop = true }
                } else {
                    gameSetupViewModel.onAction(GameSetupAction.SelectMissions(route.gameId, missionIds))
                }
            })
        }
        composable<QuestPenaltyRoute> { entry ->
            val route = entry.toRoute<QuestPenaltyRoute>()
            QuestPenaltyScreen(onNext = { penaltyId ->
                if (isHomePreview) {
                    navigation.navigate(OperationWaitingRoute(route.gameId, route.missionIds, penaltyId)) {
                        launchSingleTop = true
                    }
                } else {
                    gameSetupViewModel.onAction(
                        GameSetupAction.SelectPenalty(route.gameId, route.missionIds, penaltyId),
                    )
                }
            })
        }
        composable<OperationWaitingRoute> { entry ->
            val route = entry.toRoute<OperationWaitingRoute>()
            var isPreviewPartnerReady by rememberSaveable { mutableStateOf(false) }
            if (isHomePreview) {
                LaunchedEffect(route.gameId) {
                    if (!isPreviewPartnerReady) {
                        delay(PreviewPartnerWaitMillis)
                        isPreviewPartnerReady = true
                    }
                }
            }
            OperationWaitingScreen(
                isPartnerReady = if (isHomePreview) {
                    isPreviewPartnerReady
                } else {
                    game.gameId == route.gameId && game.gameStatus == GameStatus.PLAYING
                },
                hasStatusError = !isHomePreview && game.hasStatusError,
                onReady = {
                    navigation.navigate(OperationProgressRoute(route.gameId, route.missionIds, route.penaltyId)) {
                        popUpTo<OperationWaitingRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<OperationProgressRoute> { entry ->
            val route = entry.toRoute<OperationProgressRoute>()
            val openGuess = {
                if (entry.savedStateHandle.contains(SubmittedMissionGuessesKey)) {
                    navigation.navigate(MissionJudgementRoute(route.gameId, route.missionIds)) {
                        launchSingleTop = true
                    }
                } else {
                    navigation.navigate(MissionGuessRoute(route.gameId, route.missionIds)) { launchSingleTop = true }
                }
            }
            if (!isHomePreview) {
                val isGuessing = game.gameId == route.gameId && game.gameStatus == GameStatus.GUESSING
                LaunchedEffect(isGuessing) { if (isGuessing) openGuess() }
            }
            OperationProgressScreen(
                missionIds = route.missionIds,
                penaltyId = route.penaltyId,
                onEnd = {
                    if (isHomePreview) openGuess() else gameSetupViewModel.onAction(GameSetupAction.End(route.gameId))
                },
            )
        }
        composable<MissionGuessRoute> { entry ->
            val route = entry.toRoute<MissionGuessRoute>()
            val roundState = navigation.previousBackStackEntry?.savedStateHandle ?: entry.savedStateHandle
            if (!isHomePreview) {
                val isFinished = game.gameId == route.gameId && game.gameStatus == GameStatus.FINISHED
                LaunchedEffect(isFinished) {
                    if (isFinished) {
                        val partnerCorrectCount = roundState.get<ArrayList<Int>>(MissionJudgementsKey).orEmpty().size
                        navigation.navigate(OperationResultRoute(partnerCorrectCount)) { launchSingleTop = true }
                    }
                }
            }
            MissionGuessScreen(
                missionCount = route.missionIds.size,
                submittedAnswers = roundState.get<ArrayList<String>>(SubmittedMissionGuessesKey),
                onSubmit = { answers ->
                    roundState[SubmittedMissionGuessesKey] = ArrayList(answers)
                    navigation.navigate(MissionJudgementRoute(route.gameId, route.missionIds)) {
                        popUpTo<MissionGuessRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<MissionJudgementRoute> { entry ->
            val route = entry.toRoute<MissionJudgementRoute>()
            val roundState = navigation.previousBackStackEntry?.savedStateHandle ?: entry.savedStateHandle
            if (!isHomePreview) {
                val isFinished = game.gameId == route.gameId && game.gameStatus == GameStatus.FINISHED
                LaunchedEffect(isFinished) {
                    if (isFinished) {
                        val partnerCorrectCount = roundState.get<ArrayList<Int>>(MissionJudgementsKey).orEmpty().size
                        navigation.navigate(OperationResultRoute(partnerCorrectCount)) { launchSingleTop = true }
                    }
                }
            }
            val missionLabels = stringArrayResource(R.array.operation_sample_missions)
            val partnerGuesses = stringArrayResource(R.array.mission_judge_sample_partner_guesses)
            MissionJudgementScreen(
                ownMissions = route.missionIds.map { id -> missionLabels.getOrNull(id - 1).orEmpty() },
                partnerGuesses = partnerGuesses.take(route.missionIds.size),
                savedCorrectIndices = roundState.get<ArrayList<Int>>(MissionJudgementsKey).orEmpty(),
                onResult = { correctIndices ->
                    roundState[MissionJudgementsKey] = ArrayList(correctIndices)
                    navigation.navigate(OperationResultRoute(correctIndices.size)) {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<OperationResultRoute> { entry ->
            val route = entry.toRoute<OperationResultRoute>()
            OperationResultScreen(partnerCorrectCount = route.partnerCorrectCount, onFinish = {
                navigation.navigate(WishCouponRoute) {
                    popUpTo<TemporaryHomeRoute>()
                    launchSingleTop = true
                }
            })
        }
    }
}

/**
 * 게임 상태를 보고 다음 화면으로 넘어가야 하는 화면이면 그 게임의 ID를 반환한다.
 */
private fun NavBackStackEntry.observedGameId(): Long? {
    val destination = destination
    return when {
        destination.hasRoute<OperationWaitingRoute>() -> toRoute<OperationWaitingRoute>().gameId
        destination.hasRoute<OperationProgressRoute>() -> toRoute<OperationProgressRoute>().gameId
        destination.hasRoute<MissionGuessRoute>() -> toRoute<MissionGuessRoute>().gameId
        destination.hasRoute<MissionJudgementRoute>() -> toRoute<MissionJudgementRoute>().gameId
        else -> null
    }
}

@Composable
private fun RoomStatusScreen(message: String? = null) {
    Box(
        Modifier.fillMaxSize().background(colorResource(R.color.entrance_background)),
        contentAlignment = Alignment.Center,
    ) {
        if (message == null) CircularProgressIndicator()
        else Text(text = message, color = Color.White)
    }
}
