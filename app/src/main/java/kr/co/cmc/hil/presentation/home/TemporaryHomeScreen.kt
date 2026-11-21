package kr.co.cmc.hil.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.common.HomeStampToggle

@Composable
fun TemporaryHomeScreen(onStartMission: () -> Unit, onStamp: () -> Unit) {
    val placeholder = colorResource(R.color.temp_home_placeholder)
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.TopCenter,
    ) {
        BoxWithConstraints(Modifier.widthIn(max = 480.dp).fillMaxSize()) {
            val scale = maxWidth / 360.dp
            val contentHeight = maxOf(maxHeight, 740.dp * scale)
            Box(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = contentHeight),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    Box(Modifier.fillMaxWidth().height(24.dp * scale)
                        .background(colorResource(R.color.temp_home_top)))
                    Box(Modifier.fillMaxWidth().height(56.dp * scale)
                        .background(colorResource(R.color.temp_home_toolbar)))
                }
                Text(
                    text = stringResource(R.string.entrance_title),
                    fontSize = (20 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.offset(16.dp * scale, 93.dp * scale),
                )
                Column(
                    modifier = Modifier.align(Alignment.TopEnd)
                        .offset(x = -13.dp * scale, y = 93.dp * scale),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(Modifier.size(33.dp * scale), contentAlignment = Alignment.Center) {
                        Image(
                            painterResource(R.drawable.temp_home_help),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Text("?", fontSize = (20 * scale).sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Text(stringResource(R.string.temp_home_help), fontSize = (11 * scale).sp, color = Color.Black)
                }
                Column(
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(x = 12.dp * scale, y = 217.dp * scale)
                        .width(242.dp * scale),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(276.dp * scale)
                            .background(placeholder)
                            .clickable(role = Role.Button, onClick = onStartMission),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(stringResource(R.string.temp_home_envelope), fontSize = (20 * scale).sp, color = Color.Black)
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                            .clickable(role = Role.Button, onClick = onStartMission)
                            .padding(top = 8.dp * scale),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Text(stringResource(R.string.temp_home_start), fontSize = (20 * scale).sp, color = Color.Black)
                    }
                }
                HomeStampToggle(
                    stampSelected = false,
                    onHome = {},
                    onStamp = onStamp,
                    modifier = Modifier.align(Alignment.BottomStart)
                        .padding(start = 28.dp, bottom = 91.dp),
                )
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(56.dp * scale)
                        .background(colorResource(R.color.temp_home_bottom)),
                )
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun TemporaryHomePreview() {
    TemporaryHomeScreen(onStartMission = {}, onStamp = {})
}
