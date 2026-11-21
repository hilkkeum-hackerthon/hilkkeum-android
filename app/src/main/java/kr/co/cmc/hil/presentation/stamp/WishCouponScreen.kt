package kr.co.cmc.hil.presentation.stamp

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.model.StampsPerWish
import kr.co.cmc.hil.domain.model.WishStampProgress
import kr.co.cmc.hil.presentation.common.HomeStampToggle

private val WishFont = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
)

private const val RoadOrigin = 136f
private const val CycleSpacing = 690f

@Composable
internal fun WishCouponScreen(
    progress: WishStampProgress,
    onHome: () -> Unit,
    onUse: () -> Unit,
) {
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var showTicket by rememberSaveable { mutableStateOf(false) }

    WishCouponContent(
        progress = progress,
        onHome = onHome,
        onOpen = { showTicket = true },
        showHelp = showHelp,
        onHelp = { showHelp = !showHelp },
    )
    if (showTicket && progress.availableWishes > 0) {
        WishTicketDialog(
            onDismiss = { showTicket = false },
            onUse = {
                showTicket = false
                onUse()
            },
        )
    }
}

@Composable
private fun WishCouponContent(
    progress: WishStampProgress,
    onHome: () -> Unit,
    onOpen: () -> Unit,
    showHelp: Boolean,
    onHelp: () -> Unit,
) {
    val background = colorResource(R.color.entrance_background)
    val gradient = Brush.verticalGradient(
        0.22f to background,
        1f to colorResource(R.color.wish_coupon_background_end),
    )

    Box(Modifier.fillMaxSize().background(gradient), contentAlignment = Alignment.TopCenter) {
        BoxWithConstraints(Modifier.widthIn(max = 480.dp).fillMaxSize()) {
            val factor = maxWidth / 360.dp
            Column(Modifier.fillMaxSize()) {
                WishHeader(progress, showHelp, onHelp, factor)
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                        for (index in 0..progress.usedWishes) {
                            val isCompleted = index < progress.usedWishes
                            WishRoadCycle(
                                dates = progress.datesInCycle(index),
                                isCompleted = isCompleted,
                                isFirst = index == 0,
                                isAvailable = !isCompleted && progress.availableWishes > 0,
                                onOpen = onOpen,
                                factor = factor,
                            )
                        }
                    }
                }
            }
            HomeStampToggle(
                stampSelected = true,
                onHome = onHome,
                onStamp = {},
                modifier = Modifier.align(Alignment.BottomStart)
                    .navigationBarsPadding()
                    .padding(start = 16.dp * factor, bottom = 14.dp),
            )
        }
    }
}

@Composable
private fun WishRoadCycle(
    dates: List<String>,
    isCompleted: Boolean,
    isFirst: Boolean,
    isAvailable: Boolean,
    onOpen: () -> Unit,
    factor: Float,
) {
    val origin = if (isFirst) RoadOrigin else 0f
    val height = CycleSpacing - origin

    Box(
        Modifier.fillMaxWidth()
            .heightIn(min = (height + if (isCompleted) 0f else 160f).dp * factor),
    ) {
        if (isFirst) {
            WishImage(R.drawable.wish_route, 56f, 200f - origin, 269.431f, 425f, factor)
            WishImage(R.drawable.wish_start, 17.865f, 165.865f - origin, 76.2697f, 76.2697f, factor)
            Box(
                Modifier.offset(16.dp * factor, (164f - origin).dp * factor).size(80.dp * factor),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.wish_coupon_start),
                    style = wishStyle(20f * factor, FontWeight.SemiBold),
                    color = Color.White,
                )
            }
        } else {
            WishImage(
                R.drawable.wish_continuation, 39f, 0f, 269.431f, 425f, factor,
                modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
            )
            WishImage(R.drawable.wish_route, 56f, 200f, 269.431f, 425f, factor)
        }
        WishStamp(1, 274f, 190f - origin, R.drawable.wish_green, dates.getOrNull(0), factor)
        WishStamp(2, 79f, 350f - origin, R.drawable.wish_blue, dates.getOrNull(1), factor)
        WishStamp(3, 274f, 419f - origin, R.drawable.wish_purple, dates.getOrNull(2), factor)
        ExchangeButton(isAvailable, isCompleted, 575f - origin, onOpen, factor)
    }
}

@Composable
private fun WishHeader(progress: WishStampProgress, showHelp: Boolean, onHelp: () -> Unit, factor: Float) {
    val background = colorResource(R.color.entrance_background)

    Column(Modifier.fillMaxWidth().background(background).padding(top = 24.dp * factor)) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp * factor)
                .padding(start = 16.dp * factor, end = 4.dp * factor),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.wish_coupon_title),
                style = wishStyle(20f * factor, FontWeight.Medium),
                color = Color.White,
            )
            IconButton(onClick = onHelp, modifier = Modifier.size(maxOf(48.dp, 48.dp * factor))) {
                Image(
                    painterResource(R.drawable.wish_help),
                    stringResource(R.string.wish_coupon_help),
                    Modifier.size(24.dp * factor),
                )
            }
        }
        if (showHelp) {
            Text(
                text = stringResource(R.string.wish_coupon_help_body, StampsPerWish),
                color = colorResource(R.color.wish_coupon_label),
                style = wishStyle(12f * factor),
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp * factor, end = 16.dp * factor, bottom = 8.dp * factor)
                    .background(colorResource(R.color.wish_coupon_button_disabled), RoundedCornerShape(8.dp))
                    .padding(12.dp * factor),
            )
        } else {
            Column(
                modifier = Modifier.padding(start = 16.dp * factor, bottom = 12.dp * factor),
                verticalArrangement = Arrangement.spacedBy(4.dp * factor),
            ) {
                WishSummary(
                    stringResource(R.string.wish_coupon_stamp_label),
                    stringResource(R.string.wish_coupon_stamp_count, progress.stampCount, StampsPerWish),
                    7f, factor,
                )
                WishSummary(
                    stringResource(R.string.wish_coupon_available_label),
                    stringResource(R.string.wish_coupon_available_count, progress.availableWishes),
                    4f, factor,
                )
            }
        }
    }
}

@Composable
private fun WishSummary(label: String, value: String, gap: Float, factor: Float) {
    Row(horizontalArrangement = Arrangement.spacedBy(gap.dp * factor)) {
        Text(label, color = colorResource(R.color.wish_coupon_label), style = wishStyle(14f * factor))
        Text(
            value,
            color = colorResource(R.color.wish_coupon_value),
            style = wishStyle(14f * factor, FontWeight.Medium),
        )
    }
}

@Composable
private fun WishStamp(
    number: Int,
    center: Float,
    top: Float,
    @DrawableRes earnedImage: Int,
    date: String?,
    factor: Float,
) {
    val slotNumber = stringResource(R.string.wish_coupon_slot, number)
    if (date == null) {
        val lockedCenter = center + if (number == 3) 2.5f else 0f
        val lockedTop = top + if (number == 3) 8f else 12.5f
        WishImage(R.drawable.wish_locked, lockedCenter - 50.2675f, lockedTop + 2.23f, 100.535f, 100.535f, factor)
        WishImage(R.drawable.wish_locked_outline, lockedCenter - 44.33175f, lockedTop + 8.17f, 88.6635f, 88.6635f, factor)
        Box(
            Modifier.offset((lockedCenter - 52.5f).dp * factor, lockedTop.dp * factor)
                .size(105.dp * factor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                slotNumber,
                color = colorResource(R.color.wish_coupon_locked),
                style = wishStyle(18f * factor, FontWeight.Medium),
            )
        }
    } else {
        WishImage(earnedImage, center - 61.969f, top + 3.031f, 123.938f, 123.938f, factor)
        WishImage(R.drawable.wish_white_outline, center - 52.4355f, top + 12.5645f, 104.871f, 104.871f, factor)
        WishImage(R.drawable.wish_divider, center - 39.25f, top + 71.5f, 78.5f, 1f, factor)
        Box(
            Modifier.offset((center - 65f).dp * factor, (top + 30f).dp * factor)
                .size(130.dp * factor, 40.dp * factor),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(slotNumber, color = Color.White, style = wishStyle(28f * factor, FontWeight.SemiBold))
        }
        Box(
            Modifier.offset((center - 65f).dp * factor, (top + 80f).dp * factor)
                .size(130.dp * factor, 20.dp * factor),
            contentAlignment = Alignment.TopCenter,
        ) {
            Text(date, color = colorResource(R.color.wish_coupon_value), style = wishStyle(12f * factor))
        }
    }
}

@Composable
private fun ExchangeButton(
    isEnabled: Boolean,
    isCompleted: Boolean,
    top: Float,
    onOpen: () -> Unit,
    factor: Float,
) {
    val outerShape = RoundedCornerShape(12.dp * factor)
    val fill = colorResource(if (isEnabled) R.color.entrance_create_background else R.color.wish_coupon_button_disabled)
    val stroke = if (isEnabled) Color.White else colorResource(R.color.entrance_background)
    val text = if (isEnabled) Color.White else colorResource(R.color.wish_coupon_locked)

    Box(
        Modifier.offset(25.dp * factor, top.dp * factor)
            .size(180.dp * factor, 100.dp * factor)
            .clip(outerShape)
            .background(fill)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onOpen)
            .padding(8.dp * factor)
            .border(3.dp * factor, stroke, outerShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(
                when {
                    isCompleted -> R.string.wish_ticket_used
                    isEnabled -> R.string.wish_coupon_ready
                    else -> R.string.wish_coupon_title
                },
            ),
            color = text,
            style = wishStyle(20f * factor, FontWeight.SemiBold),
        )
    }
}

@Composable
private fun WishImage(
    @DrawableRes drawable: Int,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    factor: Float,
    modifier: Modifier = Modifier,
) {
    Image(
        painterResource(drawable),
        contentDescription = null,
        modifier = Modifier.offset(left.dp * factor, top.dp * factor)
            .size(width.dp * factor, height.dp * factor).then(modifier),
    )
}

internal fun wishStyle(size: Float, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = WishFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * 1.4f).sp,
    letterSpacing = (-size * 0.03f).sp,
)

@Preview(widthDp = 360, heightDp = 740, name = "Two stamps")
@Composable
private fun WishCouponLockedPreview() {
    WishCouponContent(WishStampProgress(listOf("2026.01.21", "2026.01.24")), {}, {}, false, {})
}

@Preview(widthDp = 360, heightDp = 740, name = "Three stamps")
@Composable
private fun WishCouponAvailablePreview() {
    WishCouponContent(WishStampProgress(listOf("2026.01.21", "2026.01.24", "2026.01.24")), {}, {}, false, {})
}
