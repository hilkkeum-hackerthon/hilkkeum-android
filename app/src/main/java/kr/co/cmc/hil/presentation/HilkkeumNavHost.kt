package kr.co.cmc.hil.presentation

import android.content.pm.ApplicationInfo
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.model.WishStampProgress
import kr.co.cmc.hil.presentation.entrance.EntranceScreen
import kr.co.cmc.hil.presentation.home.TemporaryHomeScreen
import kr.co.cmc.hil.presentation.invitation.InvitationScreen
import kr.co.cmc.hil.presentation.quest.QuestCountScreen
import kr.co.cmc.hil.presentation.quest.QuestMode
import kr.co.cmc.hil.presentation.quest.QuestModeScreen
import kr.co.cmc.hil.presentation.quest.QuestPenaltyScreen
import kr.co.cmc.hil.presentation.quest.QuestSelectionScreen
import kr.co.cmc.hil.presentation.stamp.WishCouponScreen
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

private val SampleStampDates = listOf("2026.01.21", "2026.01.24", "2026.01.24")

@Composable
fun HilkkeumNavHost() {
    val navigation = rememberNavController()
    var usedWishes by rememberSaveable { mutableIntStateOf(0) }
    val appFlags = LocalContext.current.applicationInfo.flags
    val isDebuggable = (appFlags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val startDestination = if (isDebuggable) TemporaryHomeRoute else EntranceRoute
    NavHost(navigation, startDestination = startDestination) {
        composable<TemporaryHomeRoute> {
            TemporaryHomeScreen(
                onStartMission = { navigation.navigate(QuestModeRoute) { launchSingleTop = true } },
                onStamp = { navigation.navigate(WishCouponRoute) { launchSingleTop = true } },
            )
        }
        composable<WishCouponRoute> {
            val progress = WishStampProgress(SampleStampDates, usedWishes)
            WishCouponScreen(
                progress = progress,
                onHome = { navigation.popBackStack() },
                onUse = { usedWishes = progress.useWish().usedWishes },
            )
        }
        composable<EntranceRoute> {
            EntranceScreen(
                onEnterInviteCode = { navigation.navigate(EnterInvitationRoute) },
                onCreateRoom = { navigation.navigate(ShareInvitationRoute) },
            )
        }
        composable<EnterInvitationRoute> {
            InvitationScreen(onBack = { navigation.popBackStack() })
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
        composable<QuestPenaltyRoute> {
            val context = LocalContext.current
            QuestPenaltyScreen(onNext = {
                Toast.makeText(context, R.string.quest_mode_next_pending, Toast.LENGTH_SHORT).show()
            })
        }
    }
}
