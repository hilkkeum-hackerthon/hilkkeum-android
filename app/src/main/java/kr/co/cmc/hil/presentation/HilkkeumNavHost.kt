package kr.co.cmc.hil.presentation

import android.content.pm.ApplicationInfo
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.entrance.EntranceScreen
import kr.co.cmc.hil.presentation.home.TemporaryHomeScreen
import kr.co.cmc.hil.presentation.invitation.InvitationScreen
import kr.co.cmc.hil.presentation.quest.QuestCountScreen
import kr.co.cmc.hil.presentation.quest.QuestMode
import kr.co.cmc.hil.presentation.quest.QuestModeScreen
import kr.co.cmc.hil.presentation.stamp.TemporaryStampScreen
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
private object TemporaryStampRoute

@Serializable
internal object QuestModeRoute

@Serializable
private data class QuestCountRoute(val mode: QuestMode)

@Composable
fun HilkkeumNavHost() {
    val navigation = rememberNavController()
    val appFlags = LocalContext.current.applicationInfo.flags
    val isDebuggable = (appFlags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val startDestination = if (isDebuggable) TemporaryHomeRoute else EntranceRoute
    NavHost(navigation, startDestination = startDestination) {
        composable<TemporaryHomeRoute> {
            TemporaryHomeScreen(
                onStartMission = { navigation.navigate(QuestModeRoute) { launchSingleTop = true } },
                onStamp = { navigation.navigate(TemporaryStampRoute) { launchSingleTop = true } },
            )
        }
        composable<TemporaryStampRoute> {
            TemporaryStampScreen(onHome = { navigation.popBackStack() })
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
        composable<QuestCountRoute> {
            val context = LocalContext.current
            QuestCountScreen(onNext = {
                Toast.makeText(context, R.string.quest_mode_next_pending, Toast.LENGTH_SHORT).show()
            })
        }
    }
}
