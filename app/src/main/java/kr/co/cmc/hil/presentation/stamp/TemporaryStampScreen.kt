package kr.co.cmc.hil.presentation.stamp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.common.HomeStampToggle

@Composable
fun TemporaryStampScreen(onHome: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.White)) {
        Text(
            text = stringResource(R.string.main_tab_stamp_pending),
            color = Color.Black,
            modifier = Modifier.align(Alignment.Center),
        )
        HomeStampToggle(
            stampSelected = true,
            onHome = onHome,
            onStamp = {},
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 28.dp, bottom = 91.dp),
        )
    }
}
