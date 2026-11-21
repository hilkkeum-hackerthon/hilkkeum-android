package kr.co.cmc.hil.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.common.CouponRequestBanner
import kr.co.cmc.hil.presentation.common.HomeStampToggle
import kr.co.cmc.hil.presentation.common.homeFrameScale
import kr.co.cmc.hil.presentation.quest.questTextStyle

@Composable
fun TemporaryHomeScreen(
    onStartMission: () -> Unit,
    onStamp: () -> Unit,
    hasIncomingCouponRequest: Boolean = false,
    onOpenCouponRequest: () -> Unit = {},
) {
    var isHelpOpen by rememberSaveable { mutableStateOf(false) }
    val background = colorResource(R.color.entrance_background)

    BackHandler(enabled = isHelpOpen) { isHelpOpen = false }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(background),
        contentAlignment = Alignment.Center,
    ) {
        val scale = homeFrameScale
        Box(Modifier.size(360.dp * scale, 740.dp * scale)) {
            Image(
                painter = painterResource(R.drawable.home_logo_art),
                contentDescription = stringResource(R.string.entrance_title),
                modifier = Modifier.offset(16.dp * scale, 32.dp * scale)
                    .size(61.333.dp * scale, 40.dp * scale),
            )
            MissionEnvelope(scale, onStartMission)
            Button(
                onClick = onStartMission,
                modifier = Modifier.align(Alignment.TopCenter).offset(y = 558.dp * scale)
                    .width(328.dp * scale).height(52.dp * scale),
                shape = RoundedCornerShape(8.dp * scale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.entrance_create_background),
                    contentColor = background,
                ),
            ) {
                Text(
                    text = stringResource(R.string.temp_home_start),
                    style = questTextStyle(20f * scale, FontWeight.SemiBold),
                )
            }
            if (isHelpOpen) {
                HomeHelp(scale)
            } else if (hasIncomingCouponRequest) {
                CouponRequestBanner(
                    onClick = onOpenCouponRequest,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 76.dp * scale).width(328.dp * scale),
                    scale = scale,
                )
            } else {
                // 받은 소원권 요청이 없으면 배너를 띄우지 않는다.
            }
            IconButton(
                onClick = { isHelpOpen = !isHelpOpen },
                modifier = Modifier.align(Alignment.TopEnd)
                    .offset(x = (-4).dp * scale, y = 28.dp * scale)
                    .size(48.dp * scale),
            ) {
                Image(
                    painter = painterResource(R.drawable.home_help_icon),
                    contentDescription = stringResource(
                        if (isHelpOpen) R.string.home_help_close else R.string.temp_home_help,
                    ),
                    modifier = Modifier.size(24.dp * scale),
                )
            }
        }
        HomeStampToggle(
            stampSelected = false,
            onHome = { isHelpOpen = false },
            onStamp = onStamp,
        )
    }
}

@Composable
private fun BoxScope.MissionEnvelope(scale: Float, onStartMission: () -> Unit) {
    val envelopeLabel = stringResource(R.string.temp_home_envelope)
    Box(
        Modifier.align(Alignment.TopCenter).offset(y = 124.dp * scale)
            .size(280.dp * scale, 406.dp * scale)
            .clip(RoundedCornerShape(10.769.dp * scale))
            .clickable(role = Role.Button, onClickLabel = envelopeLabel, onClick = onStartMission),
    ) {
        Image(
            painter = painterResource(R.drawable.home_envelope_art),
            contentDescription = envelopeLabel,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        Text(
            text = stringResource(R.string.home_sample_serial),
            color = colorResource(android.R.color.white),
            style = questTextStyle(24f * scale, FontWeight.SemiBold),
            modifier = Modifier.align(Alignment.TopCenter).offset(65.5.dp * scale, 295.5.dp * scale),
        )
        Text(
            text = stringResource(R.string.home_sample_date),
            color = colorResource(R.color.home_stamp_date),
            style = questTextStyle(12f * scale, FontWeight.Normal),
            modifier = Modifier.align(Alignment.TopCenter).offset(64.dp * scale, 341.5.dp * scale),
        )
    }
}

@Composable
private fun BoxScope.HomeHelp(scale: Float) {
    Column(
        modifier = Modifier.align(Alignment.TopCenter).offset(y = 77.58.dp * scale)
            .width(328.dp * scale).heightIn(max = 470.dp * scale)
            .clip(RoundedCornerShape(8.dp * scale))
            .background(colorResource(R.color.home_help_background))
            .verticalScroll(rememberScrollState())
            .padding(start = 8.dp * scale, end = 12.dp * scale, top = 12.dp * scale, bottom = 12.dp * scale),
        verticalArrangement = Arrangement.spacedBy(12.dp * scale),
    ) {
        stringArrayResource(R.array.home_help_steps).forEach { step ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp * scale)) {
                Image(
                    painter = painterResource(R.drawable.home_help_bullet),
                    contentDescription = null,
                    modifier = Modifier.size(19.dp * scale),
                )
                Text(
                    text = step,
                    color = colorResource(android.R.color.white),
                    style = questTextStyle(14f * scale, FontWeight.Normal),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    Image(
        painter = painterResource(R.drawable.home_help_pointer),
        contentDescription = null,
        modifier = Modifier.offset(313.82.dp * scale, 66.44.dp * scale)
            .size(26.058.dp * scale, 19.105.dp * scale),
    )
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun TemporaryHomePreview() {
    TemporaryHomeScreen(onStartMission = {}, onStamp = {})
}

@Preview(widthDp = 360, heightDp = 740, name = "Coupon request")
@Composable
private fun TemporaryHomeCouponRequestPreview() {
    TemporaryHomeScreen(onStartMission = {}, onStamp = {}, hasIncomingCouponRequest = true)
}
