package kr.co.cmc.hil.presentation

import android.content.pm.ApplicationInfo
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.entrance.EntranceScreen
import kr.co.cmc.hil.presentation.home.TemporaryHomeScreen
import kr.co.cmc.hil.presentation.invitation.InvitationScreen
import kr.co.cmc.hil.presentation.quest.QuestCountScreen
import kr.co.cmc.hil.presentation.quest.QuestMode
import kr.co.cmc.hil.presentation.quest.QuestModeScreen
import kr.co.cmc.hil.presentation.quest.QuestPenaltyScreen
import kr.co.cmc.hil.presentation.quest.QuestSelectionScreen
import kr.co.cmc.hil.presentation.quest.operation.MissionGuessScreen
import kr.co.cmc.hil.presentation.quest.operation.MissionJudgementScreen
import kr.co.cmc.hil.presentation.quest.operation.OperationProgressScreen
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
private data class QuestSelectionRoute(val mode: QuestMode, val missionCount: Int)

@Serializable
private data class QuestPenaltyRoute(val mode: QuestMode, val missionIds: List<Int>)

@Serializable
private data class OperationWaitingRoute(val missionIds: List<Int>, val penaltyId: Int)

@Serializable
private data class OperationProgressRoute(val missionIds: List<Int>, val penaltyId: Int)

@Serializable
private data class MissionGuessRoute(val missionIds: List<Int>)

@Serializable
private data class MissionJudgementRoute(val missionIds: List<Int>)

private const val SubmittedMissionGuessesKey = "submittedMissionGuesses"
private const val MissionJudgementsKey = "missionJudgements"

@Composable
internal fun HilkkeumNavHost(wishCouponViewModel: WishCouponViewModel) {
    val navigation = rememberNavController()
    val appFlags = LocalContext.current.applicationInfo.flags
    val isDebuggable = (appFlags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    NavHost(navigation, startDestination = EntranceRoute) {
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
                onCreateRoom = { navigation.navigate(ShareInvitationRoute) },
            )
        }
        composable<EnterInvitationRoute> {
            val sampleCode = stringResource(R.string.invitation_sample_code)
            InvitationScreen(
                onBack = { navigation.popBackStack() },
                onJoin = if (isDebuggable) ({ input ->
                    val matches = input.trim().equals(sampleCode, ignoreCase = true)
                    if (matches) {
                        navigation.navigate(TemporaryHomeRoute) {
                            popUpTo<EnterInvitationRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    matches
                }) else null,
            )
        }
        composable<ShareInvitationRoute> {
            InvitationScreen(
                onBack = { navigation.popBackStack() },
                shareCode = stringResource(R.string.invitation_sample_code),
            )
        }
        composable<QuestModeRoute> {
            QuestModeScreen(onNext = { mode ->
                navigation.navigate(QuestCountRoute(mode)) { launchSingleTop = true }
            })
        }
        composable<QuestCountRoute> { entry ->
            val mode = entry.toRoute<QuestCountRoute>().mode
            QuestCountScreen(onNext = { count ->
                navigation.navigate(QuestSelectionRoute(mode, count)) { launchSingleTop = true }
            })
        }
        composable<QuestSelectionRoute> { entry ->
            val route = entry.toRoute<QuestSelectionRoute>()
            QuestSelectionScreen(missionCount = route.missionCount, onNext = { missionIds ->
                navigation.navigate(QuestPenaltyRoute(route.mode, missionIds)) { launchSingleTop = true }
            })
        }
        composable<QuestPenaltyRoute> { entry ->
            val route = entry.toRoute<QuestPenaltyRoute>()
            QuestPenaltyScreen(onNext = { penaltyId ->
                navigation.navigate(OperationWaitingRoute(route.missionIds, penaltyId)) {
                    launchSingleTop = true
                }
            })
        }
        composable<OperationWaitingRoute> { entry ->
            val route = entry.toRoute<OperationWaitingRoute>()
            var isPartnerReady by rememberSaveable { mutableStateOf(false) }
            OperationWaitingScreen(
                isPartnerReady = isPartnerReady,
                onReady = {
                    navigation.navigate(OperationProgressRoute(route.missionIds, route.penaltyId)) {
                        popUpTo<OperationWaitingRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onDebugConfirm = if (isDebuggable) ({ isPartnerReady = true }) else null,
            )
        }
        composable<OperationProgressRoute> { entry ->
            val route = entry.toRoute<OperationProgressRoute>()
            OperationProgressScreen(
                missionIds = route.missionIds,
                penaltyId = route.penaltyId,
                onEnd = {
                    if (entry.savedStateHandle.contains(SubmittedMissionGuessesKey)) {
                        navigation.navigate(MissionJudgementRoute(route.missionIds)) { launchSingleTop = true }
                    } else {
                        navigation.navigate(MissionGuessRoute(route.missionIds)) { launchSingleTop = true }
                    }
                },
            )
        }
        composable<MissionGuessRoute> { entry ->
            val route = entry.toRoute<MissionGuessRoute>()
            val roundState = navigation.previousBackStackEntry?.savedStateHandle ?: entry.savedStateHandle
            MissionGuessScreen(
                missionCount = route.missionIds.size,
                submittedAnswers = roundState.get<ArrayList<String>>(SubmittedMissionGuessesKey),
                onSubmit = { answers ->
                    roundState[SubmittedMissionGuessesKey] = ArrayList(answers)
                    navigation.navigate(MissionJudgementRoute(route.missionIds)) {
                        popUpTo<MissionGuessRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable<MissionJudgementRoute> { entry ->
            val route = entry.toRoute<MissionJudgementRoute>()
            val roundState = navigation.previousBackStackEntry?.savedStateHandle ?: entry.savedStateHandle
            val missionLabels = stringArrayResource(R.array.operation_sample_missions)
            val partnerGuesses = stringArrayResource(R.array.mission_judge_sample_partner_guesses)
            val context = LocalContext.current
            MissionJudgementScreen(
                ownMissions = route.missionIds.map { id -> missionLabels.getOrNull(id - 1).orEmpty() },
                partnerGuesses = partnerGuesses.take(route.missionIds.size),
                savedCorrectIndices = roundState.get<ArrayList<Int>>(MissionJudgementsKey).orEmpty(),
                onResult = { correctIndices ->
                    roundState[MissionJudgementsKey] = ArrayList(correctIndices)
                    Toast.makeText(context, R.string.mission_judge_saved, Toast.LENGTH_SHORT).show()
                },
            )
        }
    }
}
