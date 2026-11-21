package kr.co.cmc.hil.presentation.quest.operation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.quest.questTextStyle

private const val SamplePlayerScore = 1
private const val SampleGradingDelayMillis = 1_500L

@Composable
internal fun OperationResultScreen(partnerCorrectCount: Int, onFinish: () -> Unit) {
    var isEnvelopeOpened by rememberSaveable { mutableStateOf(false) }
    var isSampleGradingComplete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isEnvelopeOpened, isSampleGradingComplete) {
        if (isEnvelopeOpened && !isSampleGradingComplete) {
            delay(SampleGradingDelayMillis)
            isSampleGradingComplete = true
        }
    }

    val background = colorResource(R.color.entrance_background)
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(background),
        contentAlignment = Alignment.Center,
    ) {
        val scale = minOf(maxWidth.coerceAtMost(480.dp) / 360.dp, maxHeight / 740.dp)
        Box(Modifier.size(360.dp * scale, 740.dp * scale)) {
            if (isSampleGradingComplete) {
                RevealedResult(scale, partnerCorrectCount, onFinish)
            } else {
                ResultArtwork(R.drawable.result_envelope_art)
                ResultLabel(
                    text = stringResource(
                        if (isEnvelopeOpened) R.string.operation_result_grading
                        else R.string.operation_result_open_hint,
                    ),
                    scale = scale,
                    top = 96f,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
                Box(
                    modifier = Modifier.offset(x = 32.dp * scale, y = 351.dp * scale)
                        .size(297.dp * scale, 184.dp * scale)
                        .clickable(
                            enabled = !isEnvelopeOpened,
                            role = Role.Button,
                            onClickLabel = stringResource(R.string.operation_result_open_envelope),
                            onClick = { isEnvelopeOpened = true },
                        ),
                )
            }
            Text(
                text = stringResource(R.string.operation_result_header),
                color = colorResource(android.R.color.white),
                style = questTextStyle(20f * scale, FontWeight.Medium),
                modifier = Modifier.offset(x = 16.dp * scale, y = 38.dp * scale),
            )
        }
    }
}

@Composable
private fun BoxScope.RevealedResult(scale: Float, partnerCorrectCount: Int, onFinish: () -> Unit) {
    val outcome = SamplePlayerScore.compareTo(partnerCorrectCount)
    ResultArtwork(R.drawable.result_open_art)
    ResultLabel(stringResource(R.string.operation_result_intro), scale, 112f)
    ResultLabel(
        text = stringResource(
            when {
                outcome > 0 -> R.string.operation_result_win
                outcome < 0 -> R.string.operation_result_loss
                else -> R.string.operation_result_tie
            },
        ),
        scale = scale,
        top = 209f,
        fontSize = 36f,
        weight = FontWeight.Bold,
        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
    )
    ResultLabel(
        text = stringResource(
            when {
                outcome > 0 -> R.string.operation_result_sample_stamp
                outcome < 0 -> R.string.operation_result_penalty_hint
                else -> R.string.operation_result_tie_hint
            },
        ),
        scale = scale,
        top = 267f,
    )
    when {
        outcome > 0 -> {
            ResultArtwork(R.drawable.result_stamp_art)
            ResultLabel(
                stringResource(R.string.operation_result_stamp_number), scale, 353f,
                fontSize = 32f, weight = FontWeight.SemiBold, modifier = Modifier.rotate(15f),
            )
            ResultLabel(
                text = stringResource(R.string.operation_result_stamp_date),
                scale = scale,
                top = 413f,
                fontSize = 14f,
                weight = FontWeight.Normal,
                modifier = Modifier.offset(x = (-9).dp * scale).rotate(15f),
            )
        }
        outcome < 0 -> {
            ResultArtwork(R.drawable.result_penalty_art)
            ResultLabel(stringResource(R.string.operation_result_sample_penalty), scale, 371f, fontSize = 22f)
        }
    }

    Button(
        onClick = onFinish,
        modifier = Modifier.align(Alignment.TopCenter)
            .offset(y = 572.dp * scale).width(328.dp * scale).height(52.dp * scale),
        shape = RoundedCornerShape(8.dp * scale),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.entrance_create_background),
            contentColor = colorResource(R.color.entrance_background),
        ),
    ) {
        Text(
            text = stringResource(R.string.operation_end),
            style = questTextStyle(20f * scale, FontWeight.SemiBold),
        )
    }
}

@Composable
private fun ResultArtwork(@DrawableRes artwork: Int) {
    Image(
        painter = painterResource(artwork),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds,
    )
}

@Composable
private fun BoxScope.ResultLabel(
    text: String,
    scale: Float,
    top: Float,
    fontSize: Float = 18f,
    weight: FontWeight = FontWeight.Medium,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = Modifier.align(Alignment.TopCenter).offset(y = top.dp * scale).then(modifier),
        style = questTextStyle(fontSize * scale, weight),
        textAlign = TextAlign.Center,
        color = colorResource(android.R.color.white),
    )
}
