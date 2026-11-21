package kr.co.cmc.hil.presentation.coupon.approval

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.stamp.wishStyle

@Composable
internal fun CouponApprovalScreen(
    couponId: Long,
    viewModel: CouponApprovalViewModel,
    onResponded: () -> Unit,
    onClose: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel, couponId) {
        viewModel.onAction(CouponApprovalAction.Start(couponId))
    }
    LaunchedEffect(viewModel, context) {
        viewModel.event.collect { event ->
            when (event) {
                CouponApprovalEvent.Responded -> onResponded()
                is CouponApprovalEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    CouponApprovalContent(
        uiState = uiState,
        onAction = viewModel::onAction,
        onClose = onClose,
    )
}

@Composable
private fun CouponApprovalContent(
    uiState: CouponApprovalUiState,
    onAction: (CouponApprovalAction) -> Unit,
    onClose: () -> Unit,
) {
    val orange = colorResource(R.color.entrance_create_background)
    val ink = colorResource(R.color.entrance_background)
    val ticketShape = RoundedCornerShape(14.dp)
    val message = when (uiState.result) {
        CouponApprovalResult.Approved -> R.string.coupon_approval_approved
        CouponApprovalResult.Rejected -> R.string.coupon_approval_rejected
        CouponApprovalResult.Unavailable -> R.string.coupon_approval_conflict
        null -> R.string.coupon_approval_body
    }

    Box(Modifier.fillMaxSize().background(ink), contentAlignment = Alignment.Center) {
        Column(
            Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
                .widthIn(max = 300.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                Modifier.fillMaxWidth().heightIn(min = 282.dp)
                    .clip(ticketShape).background(orange),
            ) {
                Image(
                    painterResource(R.drawable.wish_start),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomStart).offset(x = (-26).dp, y = 22.dp).size(160.dp),
                    colorFilter = ColorFilter.tint(colorResource(R.color.wish_ticket_decoration)),
                )
                Image(
                    painterResource(R.drawable.wish_start),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 38.dp, y = 10.dp).size(190.dp),
                    colorFilter = ColorFilter.tint(colorResource(R.color.wish_ticket_decoration)),
                )
                Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(start = 14.dp, top = 53.dp, end = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        stringResource(R.string.coupon_approval_title),
                        color = Color.White,
                        style = wishStyle(28f, FontWeight.SemiBold),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        stringResource(message),
                        color = Color.White,
                        style = wishStyle(18f, FontWeight.Medium),
                        textAlign = TextAlign.Center,
                    )
                }
                val closeLabel = stringResource(R.string.coupon_approval_back)
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.TopEnd).semantics { contentDescription = closeLabel },
                ) {
                    Text(stringResource(R.string.wish_ticket_close_icon), color = ink, style = wishStyle(25f))
                }
            }
            Row(
                Modifier.fillMaxWidth().heightIn(min = 72.dp)
                    .background(orange, ticketShape).padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (uiState.result == null) {
                    OutlinedButton(
                        onClick = { onAction(CouponApprovalAction.Respond(approved = false)) },
                        enabled = uiState.canRespond,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, ink),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ink, disabledContentColor = ink),
                    ) {
                        Text(stringResource(R.string.coupon_approval_reject), style = wishStyle(17f, FontWeight.Medium))
                    }
                    Button(
                        onClick = { onAction(CouponApprovalAction.Respond(approved = true)) },
                        enabled = uiState.canRespond,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ink,
                            contentColor = Color.White,
                            disabledContainerColor = ink,
                            disabledContentColor = Color.White,
                        ),
                    ) {
                        if (uiState.isSubmitting) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                        } else {
                            Text(stringResource(R.string.coupon_approval_approve), style = wishStyle(17f, FontWeight.Medium))
                        }
                    }
                } else {
                    Button(
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ink, contentColor = Color.White),
                    ) {
                        Text(stringResource(R.string.coupon_approval_confirm), style = wishStyle(17f, FontWeight.Medium))
                    }
                }
            }
            if (uiState.hasError) {
                Text(
                    stringResource(R.string.coupon_approval_failed),
                    color = colorResource(R.color.wish_coupon_value),
                    style = wishStyle(14f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
        }
    }
}

@Preview(widthDp = 360, heightDp = 740, name = "Request")
@Composable
private fun CouponApprovalRequestPreview() {
    CouponApprovalContent(CouponApprovalUiState(couponId = 1), {}, {})
}

@Preview(widthDp = 360, heightDp = 740, name = "Approved")
@Composable
private fun CouponApprovalApprovedPreview() {
    CouponApprovalContent(CouponApprovalUiState(couponId = 1, result = CouponApprovalResult.Approved), {}, {})
}
