package kr.co.cmc.hil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import kr.co.cmc.hil.presentation.HilkkeumNavHost
import kr.co.cmc.hil.presentation.invitation.RoomEntryViewModel
import kr.co.cmc.hil.presentation.quest.GameSetupViewModel
import kr.co.cmc.hil.presentation.stamp.WishCouponViewModel
import kr.co.cmc.hil.ui.theme.HilkkeumTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val wishCouponViewModel: WishCouponViewModel by viewModels()

    private val roomEntryViewModel: RoomEntryViewModel by viewModels()

    private val gameSetupViewModel: GameSetupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            HilkkeumTheme {
                HilkkeumNavHost(wishCouponViewModel, roomEntryViewModel, gameSetupViewModel, onClose = ::finish)
            }
        }
    }
}
