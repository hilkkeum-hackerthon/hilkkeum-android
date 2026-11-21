package kr.co.cmc.hil.presentation.quest.operation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.quest.questTextStyle

private const val SampleDurationMillis = (12 * 3600L + 32 * 60 + 12) * 1_000

@Composable
internal fun OperationProgressScreen(missions: List<String>, penalty: String, onEnd: () -> Unit) {
    val deadline = rememberSaveable { System.currentTimeMillis() + SampleDurationMillis }
    var remaining by rememberSaveable { mutableLongStateOf(SampleDurationMillis / 1_000) }
    var active by rememberSaveable { mutableIntStateOf(0) }
    var completed by rememberSaveable { mutableStateOf(arrayListOf<Int>()) }

    LaunchedEffect(deadline) {
        do {
            remaining = ((deadline - System.currentTimeMillis() + 999) / 1_000).coerceAtLeast(0)
            if (remaining > 0) delay(1_000)
        } while (remaining > 0)
    }

    OperationProgressContent(
        missions = missions,
        penalty = penalty,
        remainingSeconds = remaining,
        active = active,
        completed = completed,
        onChoose = { active = it },
        onComplete = {
            if (active in missions.indices && active !in completed) {
                completed = ArrayList(completed + active)
            }
        },
        onEnd = onEnd,
    )
}

@Composable
private fun OperationProgressContent(
    missions: List<String>,
    penalty: String,
    remainingSeconds: Long,
    active: Int,
    completed: List<Int>,
    onChoose: (Int) -> Unit,
    onComplete: () -> Unit,
    onEnd: () -> Unit,
) {
    val background = colorResource(R.color.entrance_background)
    val surface = colorResource(R.color.operation_surface)
    val foreground = colorResource(R.color.quest_mode_card_text)
    val orange = colorResource(R.color.entrance_create_background)
    val muted = colorResource(R.color.operation_dim_text)
    Box(Modifier.fillMaxSize().background(background), contentAlignment = Alignment.TopCenter) {
        BoxWithConstraints(Modifier.widthIn(max = 480.dp).fillMaxSize()) {
            val scale = maxWidth / 360.dp
            val shape = RoundedCornerShape(8.dp * scale)
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(start = 16.dp * scale, end = 16.dp * scale, top = 24.dp * scale, bottom = 58.dp * scale),
            ) {
                Box(Modifier.heightIn(min = 56.dp * scale), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.operation_title), color = foreground, style = questTextStyle(20f * scale, FontWeight.Medium))
                }
                Spacer(Modifier.height(8.dp * scale))
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 60.dp * scale).background(surface, shape)
                        .padding(horizontal = 16.dp * scale, vertical = 8.dp * scale),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.operation_time_label), color = colorResource(R.color.quest_count_control), style = questTextStyle(18f * scale, FontWeight.Medium))
                    Text(
                        stringResource(R.string.operation_time, remainingSeconds / 3600, remainingSeconds / 60 % 60, remainingSeconds % 60),
                        color = colorResource(R.color.wish_coupon_value),
                        style = questTextStyle(20f * scale, FontWeight.Medium),
                    )
                }
                Spacer(Modifier.height(24.dp * scale))
                Row(
                    Modifier.fillMaxWidth().heightIn(min = 28.dp * scale),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.operation_missions), color = foreground, style = questTextStyle(20f * scale, FontWeight.SemiBold))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(completed.size.toString(), color = orange, style = questTextStyle(24f * scale, FontWeight.SemiBold))
                        Text(stringResource(R.string.operation_total, missions.size), color = muted, style = questTextStyle(18f * scale, FontWeight.SemiBold))
                    }
                }
                Spacer(Modifier.height(6.dp * scale))
                Text(
                    stringResource(R.string.operation_description),
                    color = colorResource(R.color.wish_coupon_label),
                    style = questTextStyle(14f * scale, FontWeight.Normal),
                )
                Spacer(Modifier.height(18.dp * scale))
                MissionDeck(missions.getOrNull(active).orEmpty(), missions.size, active in completed, onComplete, scale)
                Spacer(Modifier.height(8.dp * scale))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp * scale, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MissionArrow(false, active > 0, { onChoose(active - 1) }, scale)
                    MissionDots(missions.size, active, scale)
                    MissionArrow(true, active < missions.lastIndex, { onChoose(active + 1) }, scale)
                }
                Spacer(Modifier.height(8.dp * scale))
                Text(stringResource(R.string.operation_penalty), color = foreground, style = questTextStyle(20f * scale, FontWeight.SemiBold))
                Spacer(Modifier.height(8.dp * scale))
                Column(
                    Modifier.fillMaxWidth().background(surface, shape)
                        .padding(start = 12.dp * scale, end = 12.dp * scale, top = 10.dp * scale, bottom = 14.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(4.dp * scale),
                ) {
                    Text(stringResource(R.string.operation_penalty_hint), color = muted, style = questTextStyle(13f * scale, FontWeight.Normal))
                    Text(penalty, color = foreground, style = questTextStyle(16f * scale, FontWeight.Medium))
                }
                Spacer(Modifier.height(18.dp * scale))
                Button(
                    onClick = onEnd,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp * scale),
                    shape = shape,
                    colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = background),
                ) {
                    Text(stringResource(R.string.operation_end), style = questTextStyle(20f * scale, FontWeight.SemiBold))
                }
            }
        }
    }
}

@Composable
private fun MissionDeck(label: String, size: Int, isComplete: Boolean, onComplete: () -> Unit, scale: Float) {
    val shape = RoundedCornerShape(10.dp * scale)
    val ink = colorResource(R.color.entrance_background)
    val cardSize = Modifier.size(280.dp * scale, 160.dp * scale)
    val blue = operationGradient(colorResource(R.color.quest_mode_together_base), colorResource(R.color.quest_mode_together_light))
    val green = operationGradient(colorResource(R.color.quest_mode_apart_base), colorResource(R.color.quest_mode_apart_light))
    val orange = operationGradient(colorResource(R.color.entrance_create_background), colorResource(R.color.operation_orange_light))

    Box(Modifier.fillMaxWidth().height(178.dp * scale)) {
        if (size >= 2) {
            Box(Modifier.align(Alignment.TopCenter).offset(19.dp * scale, 9.dp * scale)
                .then(cardSize).rotate(3.68f).alpha(0.6f).background(blue, shape))
        }
        if (size >= 3) {
            Box(Modifier.align(Alignment.TopCenter).offset((-10).dp * scale, 10.dp * scale)
                .then(cardSize).rotate(-2.24f).alpha(0.4f).background(orange, shape))
        }
        Box(
            Modifier.align(Alignment.TopCenter).offset(y = 6.dp * scale)
                .then(cardSize).background(green, shape).padding(12.dp * scale),
        ) {
            Text(
                text = label,
                color = Color.Black.copy(alpha = 0.8f),
                style = questTextStyle(20f * scale, FontWeight.Medium),
                modifier = Modifier.padding(start = 4.dp * scale, top = 4.dp * scale),
            )
            Button(
                onClick = onComplete,
                enabled = !isComplete,
                modifier = Modifier.align(Alignment.BottomEnd).heightIn(min = 45.dp * scale),
                shape = RoundedCornerShape(8.dp * scale),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ink,
                    contentColor = colorResource(R.color.quest_mode_card_text),
                    disabledContainerColor = ink.copy(alpha = 0.62f),
                    disabledContentColor = colorResource(R.color.quest_mode_card_text),
                ),
            ) {
                Text(
                    stringResource(if (isComplete) R.string.operation_completed else R.string.operation_success),
                    style = questTextStyle(16f * scale, FontWeight.Medium),
                )
            }
        }
    }
}

@Composable
private fun MissionArrow(forward: Boolean, enabled: Boolean, onClick: () -> Unit, scale: Float) {
    IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(48.dp * scale)) {
        Box(
            Modifier.size(32.dp * scale)
                .background(colorResource(R.color.operation_arrow_surface), CircleShape)
                .alpha(if (enabled) 1f else 0.45f),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(R.drawable.operation_chevron),
                stringResource(if (forward) R.string.operation_next else R.string.operation_previous),
                Modifier.size(24.dp * scale).scale(scaleX = if (forward) 1f else -1f, scaleY = 1f),
            )
        }
    }
}

@Composable
private fun MissionDots(count: Int, active: Int, scale: Float) {
    if (count == 3 && active == 1) {
        Image(painterResource(R.drawable.operation_dots), null, Modifier.size(28.dp * scale, 4.dp * scale))
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp * scale)) {
            repeat(count) { index ->
                val color = if (active == index) R.color.quest_mode_card_text else R.color.operation_idle_dot
                Box(Modifier.size(4.dp * scale).background(colorResource(color), CircleShape))
            }
        }
    }
}

private fun operationGradient(base: Color, highlight: Color) = Brush.verticalGradient(
    0f to base,
    0.5f to base,
    1f to highlight.copy(alpha = 0.6f).compositeOver(base),
)

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun OperationProgressPreview() {
    OperationProgressContent(
        missions = stringArrayResource(R.array.operation_sample_missions).take(3),
        penalty = stringArrayResource(R.array.operation_sample_penalties)[2],
        remainingSeconds = SampleDurationMillis / 1_000,
        active = 1,
        completed = listOf(0, 2),
        onChoose = {},
        onComplete = {},
        onEnd = {},
    )
}
