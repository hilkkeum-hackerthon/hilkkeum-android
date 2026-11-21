package kr.co.cmc.hil.presentation.stamp

import android.widget.Toast
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kr.co.cmc.hil.R
import kr.co.cmc.hil.domain.model.coupon.Coupon
import kr.co.cmc.hil.domain.model.coupon.CouponId
import kr.co.cmc.hil.domain.model.coupon.CouponStatus
import kr.co.cmc.hil.domain.model.room.MemberId
import kr.co.cmc.hil.domain.model.stamp.Stamps
import kr.co.cmc.hil.presentation.common.CouponRequestBanner
import kr.co.cmc.hil.presentation.common.HomeStampToggle

private val WishFont = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
)

private const val RoadOrigin = 136f
private const val CycleSpacing = 690f
private const val ButtonAreaHeight = 160f

@Composable
internal fun WishCouponScreen(
    viewModel: WishCouponViewModel,
    onHome: () -> Unit,
    onOpenCouponRequest: (Long) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showHelp by rememberSaveable { mutableStateOf(false) }
    var showTicket by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.onAction(WishCouponAction.Refresh)
    }
    LaunchedEffect(viewModel, context) {
        viewModel.event.collect { event ->
            when (event) {
                WishCouponEvent.ExchangeCompleted -> showTicket = true
                WishCouponEvent.UseRequested -> showTicket = false
                is WishCouponEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    WishCouponContent(
        uiState = uiState,
        showHelp = showHelp,
        onHelp = { showHelp = !showHelp },
        onHome = onHome,
        onCouponButton = {
            when (uiState.buttonState) {
                WishCouponButtonState.Exchangeable -> viewModel.onAction(WishCouponAction.Exchange)
                WishCouponButtonState.Usable -> {
                    viewModel.onAction(WishCouponAction.ClearUseRequestError)
                    showTicket = true
                }
                WishCouponButtonState.Locked, WishCouponButtonState.Pending -> Unit
            }
        },
        onOpenCouponRequest = {
            uiState.incomingRequest?.let { onOpenCouponRequest(it.couponId.value) }
        },
        onAction = viewModel::onAction,
    )

    val ticketCoupon = uiState.activeCoupon
    if (showTicket && ticketCoupon != null && ticketCoupon.status == CouponStatus.AVAILABLE) {
        WishTicketDialog(
            coupon = ticketCoupon,
            isSubmitting = uiState.isRequestingUse,
            hasError = uiState.hasUseRequestError,
            onDismiss = { showTicket = false },
            onUse = { viewModel.onAction(WishCouponAction.RequestUse) },
        )
    }
}

@Composable
private fun WishCouponContent(
    uiState: WishCouponUiState,
    showHelp: Boolean,
    onHelp: () -> Unit,
    onHome: () -> Unit,
    onCouponButton: () -> Unit,
    onOpenCouponRequest: () -> Unit,
    onAction: (WishCouponAction) -> Unit,
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
                WishHeader(uiState, showHelp, onHelp, factor)
                if (uiState.hasIncomingRequest) {
                    CouponRequestBanner(
                        onClick = onOpenCouponRequest,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp * factor),
                        scale = factor,
                    )
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (uiState.hasLoadError) {
                        WishLoadError(onRetry = { onAction(WishCouponAction.Refresh) }, factor = factor)
                    } else {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            WishRoad(uiState, onCouponButton, factor)
                        }
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = colorResource(R.color.entrance_create_background),
                            )
                        }
                    }
                }
            }
        }
        HomeStampToggle(
            stampSelected = true,
            onHome = onHome,
            onStamp = {},
        )
    }
}

@Composable
private fun WishRoad(uiState: WishCouponUiState, onCouponButton: () -> Unit, factor: Float) {
    val height = CycleSpacing - RoadOrigin + ButtonAreaHeight

    Box(Modifier.fillMaxWidth().heightIn(min = height.dp * factor)) {
        WishImage(R.drawable.wish_route, 56f, 200f - RoadOrigin, 269.431f, 425f, factor)
        WishImage(R.drawable.wish_start, 17.865f, 165.865f - RoadOrigin, 76.2697f, 76.2697f, factor)
        Box(
            Modifier.offset(16.dp * factor, (164f - RoadOrigin).dp * factor).size(80.dp * factor),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(R.string.wish_coupon_start),
                style = wishStyle(20f * factor, FontWeight.SemiBold),
                color = Color.White,
            )
        }
        WishStamp(1, 274f, 190f - RoadOrigin, R.drawable.wish_green, uiState.heartCount >= 1, factor)
        WishStamp(2, 79f, 350f - RoadOrigin, R.drawable.wish_blue, uiState.heartCount >= 2, factor)
        WishStamp(3, 274f, 419f - RoadOrigin, R.drawable.wish_purple, uiState.heartCount >= 3, factor)
        CouponButton(uiState.buttonState, uiState.isExchanging, 575f - RoadOrigin, onCouponButton, factor)
    }
}

@Composable
private fun WishHeader(uiState: WishCouponUiState, showHelp: Boolean, onHelp: () -> Unit, factor: Float) {
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
                text = stringResource(R.string.wish_coupon_help_body, uiState.maxHearts),
                color = colorResource(R.color.wish_coupon_label),
                style = wishStyle(12f * factor),
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 16.dp * factor, end = 16.dp * factor, bottom = 8.dp * factor)
                    .background(colorResource(R.color.wish_coupon_button_disabled), RoundedCornerShape(8.dp))
                    .padding(12.dp * factor),
            )
        } else {
            Column(
                modifier = Modifier.padding(start = 16.dp * factor, end = 16.dp * factor, bottom = 12.dp * factor),
                verticalArrangement = Arrangement.spacedBy(4.dp * factor),
            ) {
                WishSummary(
                    stringResource(R.string.wish_coupon_stamp_label),
                    stringResource(R.string.wish_coupon_stamp_count, uiState.heartCount, uiState.maxHearts),
                    7f, factor,
                )
                WishSummary(
                    stringResource(R.string.wish_coupon_available_label),
                    stringResource(R.string.wish_coupon_available_count, uiState.availableCouponCount),
                    4f, factor,
                    badge = uiState.activeCoupon?.let { dDayLabel(it.remainingDays) },
                )
                if (uiState.showFullWarning) {
                    Text(
                        text = stringResource(R.string.wish_coupon_full_warning),
                        color = colorResource(R.color.entrance_create_background),
                        style = wishStyle(12f * factor),
                    )
                }
            }
        }
    }
}

@Composable
private fun dDayLabel(remainingDays: Int): String =
    if (remainingDays > 0) {
        stringResource(R.string.wish_coupon_dday, remainingDays)
    } else {
        stringResource(R.string.wish_coupon_dday_today)
    }

@Composable
private fun WishSummary(label: String, value: String, gap: Float, factor: Float, badge: String? = null) {
    Row(horizontalArrangement = Arrangement.spacedBy(gap.dp * factor)) {
        Text(label, color = colorResource(R.color.wish_coupon_label), style = wishStyle(14f * factor))
        Text(
            value,
            color = colorResource(R.color.wish_coupon_value),
            style = wishStyle(14f * factor, FontWeight.Medium),
        )
        if (badge != null) {
            Text(
                badge,
                color = colorResource(R.color.entrance_create_background),
                style = wishStyle(14f * factor, FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun WishLoadError(onRetry: () -> Unit, factor: Float) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp * factor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            stringResource(R.string.wish_coupon_load_failed),
            color = colorResource(R.color.wish_coupon_value),
            style = wishStyle(14f * factor),
        )
        TextButton(onClick = onRetry) {
            Text(
                stringResource(R.string.wish_coupon_retry),
                color = colorResource(R.color.entrance_create_background),
                style = wishStyle(14f * factor, FontWeight.SemiBold),
            )
        }
    }
}

@Composable
private fun WishStamp(
    number: Int,
    center: Float,
    top: Float,
    @DrawableRes earnedImage: Int,
    isEarned: Boolean,
    factor: Float,
) {
    val slotNumber = stringResource(R.string.wish_coupon_slot, number)
    if (isEarned) {
        WishImage(earnedImage, center - 61.969f, top + 3.031f, 123.938f, 123.938f, factor)
        WishImage(R.drawable.wish_white_outline, center - 52.4355f, top + 12.5645f, 104.871f, 104.871f, factor)
        Box(
            Modifier.offset((center - 65f).dp * factor, (top + 3.031f).dp * factor)
                .size(130.dp * factor, 123.938.dp * factor),
            contentAlignment = Alignment.Center,
        ) {
            Text(slotNumber, color = Color.White, style = wishStyle(28f * factor, FontWeight.SemiBold))
        }
    } else {
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
    }
}

@Composable
private fun CouponButton(
    state: WishCouponButtonState,
    isExchanging: Boolean,
    top: Float,
    onClick: () -> Unit,
    factor: Float,
) {
    val isHighlighted = state == WishCouponButtonState.Exchangeable || state == WishCouponButtonState.Usable
    val isEnabled = isHighlighted && !isExchanging
    val outerShape = RoundedCornerShape(12.dp * factor)
    val fill = colorResource(if (isHighlighted) R.color.entrance_create_background else R.color.wish_coupon_button_disabled)
    val stroke = if (isHighlighted) Color.White else colorResource(R.color.entrance_background)
    val text = if (isHighlighted) Color.White else colorResource(R.color.wish_coupon_locked)
    val label = when (state) {
        WishCouponButtonState.Locked -> R.string.wish_coupon_title
        WishCouponButtonState.Exchangeable -> R.string.wish_coupon_exchange
        WishCouponButtonState.Usable -> R.string.wish_coupon_ready
        WishCouponButtonState.Pending -> R.string.wish_coupon_pending
    }

    Box(
        Modifier.offset(25.dp * factor, top.dp * factor)
            .size(180.dp * factor, 100.dp * factor)
            .clip(outerShape)
            .background(fill)
            .clickable(enabled = isEnabled, role = Role.Button, onClick = onClick)
            .padding(8.dp * factor)
            .border(3.dp * factor, stroke, outerShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isExchanging) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(28.dp * factor))
        } else {
            Text(stringResource(label), color = text, style = wishStyle(20f * factor, FontWeight.SemiBold))
        }
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

private fun previewState(heartCount: Int, coupon: Coupon? = null): WishCouponUiState = WishCouponUiState(
    isLoading = false,
    stamps = Stamps(
        memberId = MemberId(1),
        heartCount = heartCount,
        maxHearts = DefaultMaxHearts,
        canExchange = heartCount == DefaultMaxHearts && coupon == null,
        hasActiveCoupon = coupon != null,
    ),
    coupon = coupon,
)

private fun previewCoupon(status: CouponStatus): Coupon {
    val issuedAt = OffsetDateTime.of(2026, 11, 21, 20, 0, 0, 0, ZoneOffset.ofHours(9))

    return Coupon(
        couponId = CouponId(1),
        status = status,
        issuedAt = issuedAt,
        expiresAt = issuedAt.plusMonths(1),
        remainingDays = 30,
    )
}

@Preview(widthDp = 360, heightDp = 740, name = "Two stamps")
@Composable
private fun WishCouponLockedPreview() {
    WishCouponContent(previewState(2), false, {}, {}, {}, {}, {})
}

@Preview(widthDp = 360, heightDp = 740, name = "Exchangeable")
@Composable
private fun WishCouponExchangeablePreview() {
    WishCouponContent(previewState(3), false, {}, {}, {}, {}, {})
}

@Preview(widthDp = 360, heightDp = 740, name = "Coupon pending, full warning")
@Composable
private fun WishCouponPendingPreview() {
    WishCouponContent(previewState(3, previewCoupon(CouponStatus.PENDING)), false, {}, {}, {}, {}, {})
}
