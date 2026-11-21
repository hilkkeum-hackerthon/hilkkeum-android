package kr.co.cmc.hil.presentation.quest.operation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.quest.questTextStyle

@Composable
internal fun OperationWaitingScreen(
    isPartnerReady: Boolean,
    onReady: () -> Unit,
    onDebugConfirm: (() -> Unit)? = null,
    hasStatusError: Boolean = false,
) {
    LaunchedEffect(isPartnerReady) {
        if (isPartnerReady) onReady()
    }

    val background = colorResource(R.color.entrance_background)
    BoxWithConstraints(
        Modifier.fillMaxSize().background(
            Brush.verticalGradient(0.22f to background, 1f to colorResource(R.color.wish_coupon_background_end)),
        ),
    ) {
        val scale = maxWidth.coerceAtMost(480.dp) / 360.dp
        val debugLabel = stringResource(R.string.operation_debug_ready)
        // Long-press is only wired by the debug navigation flow.
        val indicatorAction = if (onDebugConfirm == null) Modifier else Modifier.combinedClickable(
            onClickLabel = debugLabel,
            onLongClickLabel = debugLabel,
            onLongClick = onDebugConfirm,
            onClick = {},
        )
        Box(
            modifier = Modifier.align(Alignment.TopCenter)
                .offset(y = 96.dp * scale - 24.dp)
                .size(64.dp * scale, 48.dp)
                .then(indicatorAction),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(R.drawable.quest_mode_progress),
                contentDescription = null,
                modifier = Modifier.size(40.dp * scale, 8.dp * scale),
            )
        }
        Text(
            text = stringResource(R.string.operation_waiting),
            color = colorResource(R.color.quest_mode_title),
            style = questTextStyle(22f * scale, FontWeight.Bold),
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.TopCenter)
                .offset(y = 120.dp * scale).padding(horizontal = 16.dp),
        )
    }
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun OperationWaitingPreview() {
    OperationWaitingScreen(isPartnerReady = false, onReady = {})
}
