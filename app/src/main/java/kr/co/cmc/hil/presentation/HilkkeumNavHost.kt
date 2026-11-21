package kr.co.cmc.hil.presentation

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import kr.co.cmc.hil.presentation.coupon.approval.CouponApprovalScreen
import kr.co.cmc.hil.presentation.coupon.approval.CouponApprovalViewModel
import kr.co.cmc.hil.presentation.entrance.EntranceScreen
import kr.co.cmc.hil.presentation.home.TemporaryHomeScreen
import kr.co.cmc.hil.presentation.howto.HowToScreen
import kr.co.cmc.hil.presentation.invitation.InvitationScreen
import kr.co.cmc.hil.presentation.invitation.RoomEntryAction
import kr.co.cmc.hil.presentation.invitation.RoomEntryViewModel
import kr.co.cmc.hil.presentation.quest.GameSetupAction
import kr.co.cmc.hil.presentation.quest.GameSetupEvent
import kr.co.cmc.hil.presentation.quest.GameSetupViewModel
import kr.co.cmc.hil.presentation.quest.QuestCountScreen
import kr.co.cmc.hil.presentation.quest.QuestMode
import kr.co.cmc.hil.presentation.quest.QuestModeScreen
import kr.co.cmc.hil.domain.model.game.DateType
import kr.co.cmc.hil.presentation.quest.QuestPenaltyScreen
import kr.co.cmc.hil.presentation.quest.QuestSelectionScreen
import kr.co.cmc.hil.presentation.quest.operation.MissionGuessScreen
import kr.co.cmc.hil.presentation.quest.operation.MissionJudgementScreen
import kr.co.cmc.hil.presentation.quest.operation.OperationProgressScreen
import kr.co.cmc.hil.presentation.quest.operation.OperationResultScreen
import kr.co.cmc.hil.presentation.quest.operation.OperationWaitingScreen
import kr.co.cmc.hil.presentation.stamp.WishCouponAction
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
private object HowToRoute

@Serializable
private object WishCouponRoute

@Serializable
private data class CouponApprovalRoute(val couponId: Long)

@Serializable
internal object QuestModeRoute

@Serializable
private data class QuestCountRoute(val mode: QuestMode)

@Serializable
private data class QuestSelectionRoute(val gameId: Long, val missionCount: Int, val dateType: String)

@Serializable
private data class QuestPenaltyRoute(val gameId: Long, val missionLabels: List<String>)

@Serializable
private data class OperationWaitingRoute(val gameId: Long, val missionLabels: List<String>, val penaltyLabel: String)

@Serializable
private data class OperationProgressRoute(val gameId: Long, val missionLabels: List<String>, val penaltyLabel: String)

@Serializable
private data class MissionGuessRoute(val gameId: Long, val missionLabels: List<String>)

@Serializable
private data class MissionJudgementRoute(val gameId: Long, val missionLabels: List<String>)

@Serializable
private data class OperationResultRoute(val gameId: Long, val partnerCorrectCount: Int = 0)

private const val PreviewGameId = 0L
private const val PreviewPartnerWaitMillis = 1_500L

private const val SubmittedMissionGuessesKey = "submittedMissionGuesses"
private const val MissionJudgementsKey = "missionJudgements"

@Composable
internal fun HilkkeumNavHost(
    wishCouponViewModel: WishCouponViewModel,
    couponApprovalViewModel: CouponApprovalViewModel,
    roomEntryViewModel: RoomEntryViewModel,
    gameSetupViewModel: GameSetupViewModel,
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
    val wishCoupon by wishCouponViewModel.uiState.collectAsStateWithLifecycle()
    val currentEntry by navigation.currentBackStackEntryAsState()
    val currentDestination = currentEntry?.destination
    val isRewardVisible = !isHomePreview && (
        currentDestination?.hasRoute<TemporaryHomeRoute>() == true ||
            currentDestination?.hasRoute<WishCouponRoute>() == true
        )
    val openCouponApproval = { couponId: Long ->
        navigation.navigate(CouponApprovalRoute(couponId)) { launchSingleTop = true }
    }
    // 홈·스탬프 화면이 보이는 동안만 받은 소원권 요청과 승인 대기 상태를 폴링한다.
    LifecycleStartEffect(isRewardVisible) {
        wishCouponViewModel.onAction(WishCouponAction.Observe(isRewardVisible))
        onStopOrDispose { wishCouponViewModel.onAction(WishCouponAction.Observe(false)) }
    }
    LaunchedEffect(navigation, gameSetupViewModel, context) {
        gameSetupViewModel.event.collect { event ->
            when (event) {
                is GameSetupEvent.OpenSelection -> {
                    if (navigation.currentDestination?.hasRoute<QuestCountRoute>() == true) {
                        navigation.navigate(QuestSelectionRoute(event.gameId, event.count, event.dateType.name)) {
                            popUpTo<QuestModeRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                is GameSetupEvent.MissionsSubmitted -> {
                    val entry = navigation.currentBackStackEntry
                    if (entry?.destination?.hasRoute<QuestSelectionRoute>() == true) {
                        val route = entry.toRoute<QuestSelectionRoute>()
                        navigation.navigate(QuestPenaltyRoute(route.gameId, event.labels)) { launchSingleTop = true }
                    }
                }
                is GameSetupEvent.PenaltySubmitted -> {
                    val entry = navigation.currentBackStackEntry
                    if (entry?.destination?.hasRoute<QuestPenaltyRoute>() == true) {
                        val route = entry.toRoute<QuestPenaltyRoute>()
                        navigation.navigate(OperationWaitingRoute(route.gameId, route.missionLabels, event.label)) {
                            launchSingleTop = true
                        }
                    }
                }
                GameSetupEvent.GuessesSubmitted -> {
                    val entry = navigation.currentBackStackEntry
                    if (entry?.destination?.hasRoute<MissionGuessRoute>() == true) {
                        val route = entry.toRoute<MissionGuessRoute>()
                        navigation.navigate(MissionJudgementRoute(route.gameId, route.missionLabels)) {
                            popUpTo<MissionGuessRoute> { inclusive = true }
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
                onHowTo = { navigation.navigate(HowToRoute) { launchSingleTop = true } },
                onStamp = { navigation.navigate(WishCouponRoute) { launchSingleTop = true } },
                hasIncomingCouponRequest = wishCoupon.hasIncomingRequest,
                onOpenCouponRequest = {
                    wishCoupon.incomingRequest?.let { openCouponApproval(it.couponId.value) }
                },
            )
        }
        composable<HowToRoute> {
            HowToScreen(onBack = { navigation.popBackStack() })
        }
        composable<WishCouponRoute> {
            WishCouponScreen(
                viewModel = wishCouponViewModel,
                onHome = { navigation.popBackStack() },
                onOpenCouponRequest = openCouponApproval,
            )
        }
        composable<CouponApprovalRoute> { entry ->
            val route = entry.toRoute<CouponApprovalRoute>()
            CouponApprovalScreen(
                couponId = route.couponId,
                viewModel = couponApprovalViewModel,
                onResponded = { wishCouponViewModel.onAction(WishCouponAction.Refresh) },
                onClose = { navigation.popBackStack() },
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
                    val leaveRoom = { roomEntryViewModel.onAction(RoomEntryAction.Leave) }
                    // 상대가 들어오기 전 뒤로 가면 방을 삭제하고 첫 화면으로 돌아간다(세션이 비면 위의 LaunchedEffect가 이동).
                    BackHandler(onBack = leaveRoom)
                    InvitationScreen(
                        onBack = leaveRoom,
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
                    navigation.navigate(QuestSelectionRoute(PreviewGameId, count, DateType.OFFLINE.name)) {
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
            val sampleLabels = stringArrayResource(R.array.operation_sample_missions).toList()
            val labels = if (isHomePreview) sampleLabels else game.missions?.map { it.content }
            val load = { gameSetupViewModel.onAction(GameSetupAction.LoadMissions(DateType.valueOf(route.dateType))) }
            LaunchedEffect(route) { if (!isHomePreview) load() }
            val status: (@Composable () -> Unit)? = if (labels == null || labels.size < route.missionCount) {
                { CatalogStatusContent(game.hasCatalogError || labels != null, load) }
            } else null
            QuestSelectionScreen(route.missionCount, labels.orEmpty(), game.isLoading, status) { indices ->
                if (isHomePreview) {
                    navigation.navigate(QuestPenaltyRoute(route.gameId, indices.map(sampleLabels::get))) {
                        launchSingleTop = true
                    }
                } else {
                    gameSetupViewModel.onAction(GameSetupAction.SubmitMissions(route.gameId, indices))
                }
            }
        }
        composable<QuestPenaltyRoute> { entry ->
            val route = entry.toRoute<QuestPenaltyRoute>()
            val samples = stringArrayResource(R.array.operation_sample_penalties).toList()
            val load = { gameSetupViewModel.onAction(GameSetupAction.LoadPenalties) }
            LaunchedEffect(route) { if (!isHomePreview) load() }
            val labels = if (isHomePreview) samples else game.penalties?.map { it.content }
            val status: (@Composable () -> Unit)? = if (labels.isNullOrEmpty()) {
                { CatalogStatusContent(game.hasCatalogError || labels != null, load) }
            } else null
            QuestPenaltyScreen(labels.orEmpty(), game.isLoading, status) { index ->
                if (isHomePreview) {
                    navigation.navigate(OperationWaitingRoute(route.gameId, route.missionLabels, samples[index])) {
                        launchSingleTop = true
                    }
                } else {
                    gameSetupViewModel.onAction(GameSetupAction.SubmitPenalty(route.gameId, index))
                }
            }
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
            } else {
                LifecycleStartEffect(route.gameId) {
                    gameSetupViewModel.onAction(GameSetupAction.Observe(route.gameId))
                    onStopOrDispose { gameSetupViewModel.onAction(GameSetupAction.Observe(null)) }
                }
            }
            OperationWaitingScreen(
                isPartnerReady = if (isHomePreview) isPreviewPartnerReady else game.playingGameId == route.gameId,
                hasStatusError = !isHomePreview && game.hasStatusError,
                onReady = {
                    navigation.navigate(OperationProgressRoute(route.gameId, route.missionLabels, route.penaltyLabel)) {
                        popUpTo<OperationWaitingRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<OperationProgressRoute> { entry ->
            val route = entry.toRoute<OperationProgressRoute>()
            if (!isHomePreview) {
                LifecycleStartEffect(route.gameId) {
                    gameSetupViewModel.onAction(GameSetupAction.Observe(route.gameId))
                    onStopOrDispose { gameSetupViewModel.onAction(GameSetupAction.Observe(null)) }
                }
                LaunchedEffect(game.guessingGameId, game.finishedGameId) {
                    if (game.finishedGameId == route.gameId) {
                        navigation.navigate(OperationResultRoute(route.gameId)) { launchSingleTop = true }
                    } else if (game.guessingGameId == route.gameId) {
                        navigation.navigate(MissionGuessRoute(route.gameId, route.missionLabels)) {
                            popUpTo<OperationProgressRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
            OperationProgressScreen(
                missions = route.missionLabels,
                penalty = route.penaltyLabel,
                onEnd = {
                    if (!isHomePreview) {
                        gameSetupViewModel.onAction(GameSetupAction.End(route.gameId))
                    } else if (entry.savedStateHandle.contains(SubmittedMissionGuessesKey)) {
                        navigation.navigate(MissionJudgementRoute(route.gameId, route.missionLabels)) { launchSingleTop = true }
                    } else {
                        navigation.navigate(MissionGuessRoute(route.gameId, route.missionLabels)) { launchSingleTop = true }
                    }
                },
            )
        }
        composable<MissionGuessRoute> { entry ->
            val route = entry.toRoute<MissionGuessRoute>()
            val roundState = navigation.previousBackStackEntry?.savedStateHandle ?: entry.savedStateHandle
            ObserveRoundForResult(route.gameId, gameSetupViewModel, isHomePreview, isFinished = game.finishedGameId == route.gameId) {
                navigation.navigate(OperationResultRoute(route.gameId)) { launchSingleTop = true }
            }
            MissionGuessScreen(
                missionCount = route.missionLabels.size,
                submittedAnswers = roundState.get<ArrayList<String>>(SubmittedMissionGuessesKey),
                isSubmitting = !isHomePreview && game.isLoading,
                onSubmit = { answers ->
                    if (isHomePreview) {
                        roundState[SubmittedMissionGuessesKey] = ArrayList(answers)
                        navigation.navigate(MissionJudgementRoute(route.gameId, route.missionLabels)) {
                            popUpTo<MissionGuessRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        gameSetupViewModel.onAction(GameSetupAction.SubmitGuesses(route.gameId, answers))
                    }
                },
            )
        }
        composable<MissionJudgementRoute> { entry ->
            val route = entry.toRoute<MissionJudgementRoute>()
            val roundState = navigation.previousBackStackEntry?.savedStateHandle ?: entry.savedStateHandle
            ObserveRoundForResult(route.gameId, gameSetupViewModel, isHomePreview, fetchGuesses = true,
                isFinished = game.finishedGameId == route.gameId) {
                navigation.navigate(OperationResultRoute(route.gameId)) { launchSingleTop = true }
            }
            val received = game.receivedGuesses?.takeIf { it.gameId.value == route.gameId }
            val partnerGuesses = if (isHomePreview) {
                stringArrayResource(R.array.mission_judge_sample_partner_guesses).take(route.missionLabels.size)
            } else {
                received?.takeIf { it.partnerSubmissionCompleted }?.guesses?.map { it.content }.orEmpty()
            }
            if (!isHomePreview && game.judgedGameId == route.gameId) {
                RoomStatusScreen(stringResource(R.string.operation_result_grading))
            } else if (partnerGuesses.isEmpty()) {
                RoomStatusScreen(stringResource(
                    if (game.hasStatusError) R.string.game_status_failed else R.string.game_wait_for_guesses,
                ))
            } else {
                MissionJudgementScreen(
                    ownMissions = route.missionLabels,
                    partnerGuesses = partnerGuesses,
                    savedCorrectIndices = roundState.get<ArrayList<Int>>(MissionJudgementsKey).orEmpty(),
                    onResult = { correctIndices ->
                        if (isHomePreview) {
                            roundState[MissionJudgementsKey] = ArrayList(correctIndices)
                            navigation.navigate(OperationResultRoute(route.gameId, correctIndices.size)) {
                                launchSingleTop = true
                            }
                        } else {
                            gameSetupViewModel.onAction(GameSetupAction.Judge(route.gameId, correctIndices))
                        }
                    },
                )
            }
        }
        composable<OperationResultRoute> { entry ->
            val route = entry.toRoute<OperationResultRoute>()
            val result = game.result?.takeIf { it.gameId.value == route.gameId }
            val load = { gameSetupViewModel.onAction(GameSetupAction.LoadResult(route.gameId)) }
            LaunchedEffect(route) { if (!isHomePreview) load() }
            if (!isHomePreview && result == null) {
                CatalogStatusScreen(game.hasResultError, load)
            } else {
                OperationResultScreen(
                    partnerCorrectCount = route.partnerCorrectCount,
                    result = result,
                    onFinish = {
                        navigation.navigate(WishCouponRoute) {
                            popUpTo<TemporaryHomeRoute>()
                            launchSingleTop = true
                        }
                    },
                )
            }
        }
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

@Composable
private fun CatalogStatusScreen(hasError: Boolean, onRetry: () -> Unit) {
    if (hasError) {
        Box(
            Modifier.fillMaxSize().background(colorResource(R.color.entrance_background)),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Button(onClick = onRetry) {
                Text(stringResource(R.string.game_catalog_retry))
            }
        }
    } else {
        RoomStatusScreen()
    }
}

@Composable
private fun ObserveRoundForResult(
    gameId: Long,
    viewModel: GameSetupViewModel,
    isPreview: Boolean,
    fetchGuesses: Boolean = false,
    isFinished: Boolean,
    onFinished: () -> Unit,
) {
    if (!isPreview) {
        LifecycleStartEffect(gameId, fetchGuesses) {
            viewModel.onAction(GameSetupAction.Observe(gameId, fetchGuesses))
            onStopOrDispose { viewModel.onAction(GameSetupAction.Observe(null)) }
        }
        LaunchedEffect(isFinished) { if (isFinished) onFinished() }
    }
}


@Composable
private fun CatalogStatusContent(hasError: Boolean, onRetry: () -> Unit) {
    if (hasError) {
        androidx.compose.material3.Button(onClick = onRetry) {
            Text(stringResource(R.string.game_catalog_retry))
        }
    } else {
        CircularProgressIndicator()
    }
}
