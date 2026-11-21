package kr.co.cmc.hil.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.entrance.EntranceScreen
import kr.co.cmc.hil.presentation.invitation.InvitationScreen
import kotlinx.serialization.Serializable

@Serializable
private object EntranceRoute

@Serializable
private object EnterInvitationRoute

@Serializable
private object ShareInvitationRoute

@Composable
fun HilkkeumNavHost() {
    val navigation = rememberNavController()
    NavHost(navigation, startDestination = EntranceRoute) {
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
    }
}
