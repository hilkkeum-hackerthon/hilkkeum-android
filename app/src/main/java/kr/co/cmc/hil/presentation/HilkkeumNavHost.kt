package kr.co.cmc.hil.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
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
import kr.co.cmc.hil.presentation.room.RoomAction
import kr.co.cmc.hil.presentation.room.RoomViewModel
import kr.co.cmc.hil.presentation.room.roomMessage
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
fun HilkkeumNavHost(room: RoomViewModel) {
    val navigation = rememberNavController()
    val state by room.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navigation.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val snackbar = remember { SnackbarHostState() }
    val error = state.requestError?.let {
        stringResource(it.roomMessage(isJoining = destination?.hasRoute<EnterInvitationRoute>() == true))
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbar.showSnackbar(error)
            room.onAction(RoomAction.DismissRequestError)
        }
    }
    LaunchedEffect(state.isRestoring, state.roomId, state.isPaired) {
        if (!state.isRestoring && state.roomId != null) {
            if (state.isPaired) {
                navigation.navigate(TemporaryHomeRoute) {
                    popUpTo(navigation.graph.id)
                    launchSingleTop = true
                }
            } else if (navigation.currentDestination?.hasRoute<ShareInvitationRoute>() != true) {
                navigation.navigate(ShareInvitationRoute) {
                    popUpTo(navigation.graph.id)
                    launchSingleTop = true
                }
            }
        } else if (!state.isRestoring && navigation.currentDestination?.hasRoute<EntranceRoute>() != true) {
            navigation.navigate(EntranceRoute) {
                popUpTo(navigation.graph.id)
                launchSingleTop = true
            }
        }
    }

    Box(Modifier.fillMaxSize().background(colorResource(R.color.entrance_background))) {
        NavHost(navigation, startDestination = EntranceRoute) {
            composable<TemporaryHomeRoute> {
                if (state.isPaired) {
                    TemporaryHomeScreen(
                        onStartMission = { navigation.navigate(QuestModeRoute) { launchSingleTop = true } },
                        onStamp = { navigation.navigate(TemporaryStampRoute) { launchSingleTop = true } },
                    )
                }
            }
            composable<TemporaryStampRoute> {
                TemporaryStampScreen(onHome = { navigation.popBackStack() })
            }
            composable<EntranceRoute> {
                if (!state.isRestoring) {
                    EntranceScreen(
                        onEnterInviteCode = {
                            if (!state.isSubmitting) {
                                if (state.roomId == null) {
                                    navigation.navigate(EnterInvitationRoute) { launchSingleTop = true }
                                } else {
                                    navigation.navigate(ShareInvitationRoute) { launchSingleTop = true }
                                }
                            }
                        },
                        onCreateRoom = {
                            if (state.roomId == null) {
                                room.onAction(RoomAction.Create)
                            } else {
                                navigation.navigate(ShareInvitationRoute) { launchSingleTop = true }
                            }
                        },
                    )
                }
            }
            composable<EnterInvitationRoute> {
                InvitationScreen(
                    onBack = { if (!state.isSubmitting) navigation.popBackStack() },
                    onJoin = { room.onAction(RoomAction.Join(it)) },
                    isSubmitting = state.isSubmitting,
                )
            }
            composable<ShareInvitationRoute> {
                val inviteCode = state.inviteCode
                if (inviteCode != null) {
                    LifecycleStartEffect(state.roomId) {
                        room.onAction(RoomAction.StartPolling)
                        onStopOrDispose { room.onAction(RoomAction.StopPolling) }
                    }
                    val waitingMessage = stringResource(
                        state.statusError?.roomMessage() ?: if (state.isCreator) R.string.room_waiting
                        else R.string.room_checking,
                    )
                    InvitationScreen(
                        onBack = {
                            if (!navigation.popBackStack()) {
                                navigation.navigate(EntranceRoute) {
                                    popUpTo(navigation.graph.id)
                                    launchSingleTop = true
                                }
                            }
                        },
                        shareCode = inviteCode,
                        waitingMessage = waitingMessage,
                    )
                }
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
        if (state.isRestoring || state.isSubmitting) {
            LinearProgressIndicator(Modifier.align(Alignment.TopCenter).fillMaxWidth())
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
    }
}
