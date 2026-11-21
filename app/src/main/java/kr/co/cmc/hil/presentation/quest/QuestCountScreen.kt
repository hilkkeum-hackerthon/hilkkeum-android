package kr.co.cmc.hil.presentation.quest

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.cmc.hil.R

private const val MinimumMissionCount = 1
private const val MaximumMissionCount = 3

@Composable
fun QuestCountScreen(onNext: (Int) -> Unit) {
    var count by rememberSaveable { mutableIntStateOf(MaximumMissionCount) }
    QuestCountContent(
        count = count,
        onDecrease = { count = (count - 1).coerceAtLeast(MinimumMissionCount) },
        onIncrease = { count = (count + 1).coerceAtMost(MaximumMissionCount) },
        onNext = { onNext(count) },
    )
}

@Composable
private fun QuestCountContent(
    count: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onNext: () -> Unit,
) {
    val background = colorResource(R.color.entrance_background)
    Box(
        Modifier.fillMaxSize().background(background),
        contentAlignment = Alignment.TopCenter,
    ) {
        BoxWithConstraints(Modifier.widthIn(max = 480.dp).fillMaxSize()) {
            val scale = maxWidth / 360.dp
            val contentHeight = maxOf(maxHeight, 740.dp * scale)
            Box(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = contentHeight),
            ) {
                Image(
                    painter = painterResource(R.drawable.quest_count_progress),
                    contentDescription = stringResource(R.string.quest_count_progress),
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(y = 92.dp * scale).size(40.dp * scale, 8.dp * scale),
                )
                Text(
                    text = stringResource(R.string.quest_count_title),
                    color = colorResource(R.color.quest_mode_title),
                    style = questTextStyle(22f * scale, FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 120.dp * scale)
                        .padding(horizontal = 16.dp * scale),
                )
                Image(
                    painter = painterResource(R.drawable.quest_count_glow),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(x = -5.dp * scale, y = 263.dp * scale).size(284.dp * scale),
                )
                QuestFolder(R.drawable.quest_count_orange, -65f, 296.19f, -8.05f, scale)
                QuestFolder(R.drawable.quest_count_pink, 64.60f, 268.64f, 23.86f, scale)
                QuestFolder(R.drawable.quest_count_green, 1.60f, 349.64f, 7.59f, scale)

                CountControl(
                    icon = R.drawable.quest_count_minus,
                    label = R.string.quest_count_decrease,
                    enabled = count > MinimumMissionCount,
                    onClick = onDecrease,
                    scale = scale,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(x = -85.dp * scale, y = 525.dp * scale - controlTargetSize(scale) / 2),
                )
                Text(
                    text = stringResource(R.string.quest_count_value, count),
                    color = colorResource(R.color.quest_mode_card_text),
                    style = questTextStyle(28f * scale, FontWeight.Bold),
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(x = 2.dp * scale, y = 505.dp * scale),
                )
                CountControl(
                    icon = R.drawable.quest_count_plus,
                    label = R.string.quest_count_increase,
                    enabled = count < MaximumMissionCount,
                    onClick = onIncrease,
                    scale = scale,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(x = 89.dp * scale, y = 525.dp * scale - controlTargetSize(scale) / 2),
                )
                Button(
                    onClick = onNext,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .offset(x = 4.dp * scale)
                        .padding(bottom = 78.dp * scale)
                        .width(328.dp * scale)
                        .heightIn(min = maxOf(48.dp, 52.dp * scale)),
                    shape = RoundedCornerShape(8.dp * scale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.entrance_create_background),
                        contentColor = background,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.quest_mode_next),
                        style = questTextStyle(20f * scale, FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.QuestFolder(
    @DrawableRes artwork: Int,
    centerOffset: Float,
    top: Float,
    rotation: Float,
    scale: Float,
) {
    Image(
        painter = painterResource(artwork),
        contentDescription = null,
        modifier = Modifier.align(Alignment.TopCenter)
            .offset(x = centerOffset.dp * scale, y = top.dp * scale)
            .size(150.dp * scale, 110.dp * scale)
            .rotate(rotation),
    )
}

@Composable
private fun CountControl(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    scale: Float,
    modifier: Modifier,
) {
    IconButton(onClick = onClick, enabled = enabled, modifier = modifier.size(controlTargetSize(scale))) {
        Box(
            Modifier.size(44.dp * scale)
                .background(colorResource(R.color.quest_count_control), RoundedCornerShape(4.dp * scale)),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(icon), stringResource(label), Modifier.size(33.dp * scale))
        }
    }
}

private fun controlTargetSize(scale: Float) = maxOf(48.dp, 44.dp * scale)

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun QuestCountPreview() {
    QuestCountScreen(onNext = {})
}
