package kr.co.cmc.hil.presentation.quest.operation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.quest.questTextStyle

@Composable
internal fun MissionJudgementScreen(
    ownMissions: List<String>,
    partnerGuesses: List<String>,
    savedCorrectIndices: List<Int>,
    onResult: (List<Int>) -> Unit,
) {
    var correctIndices by rememberSaveable { mutableStateOf(ArrayList(savedCorrectIndices)) }
    var inspectingIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    MissionJudgementContent(
        partnerGuesses = partnerGuesses,
        correctIndices = correctIndices,
        onToggle = { index, checked ->
            val updated = if (checked) correctIndices + index else correctIndices - index
            correctIndices = ArrayList(updated)
        },
        onInspect = { inspectingIndex = it },
        onResult = { onResult(correctIndices.toList()) },
    )
    val ownMission = inspectingIndex?.let(ownMissions::getOrNull)
    if (ownMission != null) {
        AlertDialog(
            onDismissRequest = { inspectingIndex = null },
            title = { Text(stringResource(R.string.mission_judge_show_mission)) },
            text = { Text(ownMission) },
            confirmButton = {
                TextButton(onClick = { inspectingIndex = null }) {
                    Text(stringResource(R.string.wish_coupon_close))
                }
            },
        )
    }
}

@Composable
private fun MissionJudgementContent(
    partnerGuesses: List<String>,
    correctIndices: List<Int>,
    onToggle: (Int, Boolean) -> Unit,
    onInspect: (Int) -> Unit,
    onResult: () -> Unit,
) {
    val background = colorResource(R.color.entrance_background)
    val foreground = colorResource(R.color.quest_mode_card_text)

    Box(Modifier.fillMaxSize().background(background), contentAlignment = Alignment.TopCenter) {
        BoxWithConstraints(Modifier.widthIn(max = 480.dp).fillMaxSize().navigationBarsPadding()) {
            val scale = maxWidth / 360.dp
            val contentHeight = maxOf(maxHeight, 714.dp * scale)
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = contentHeight)
                    .padding(start = 16.dp * scale, end = 16.dp * scale, top = 38.dp * scale, bottom = 32.dp * scale),
            ) {
                Text(
                    stringResource(R.string.mission_guess_title),
                    color = foreground,
                    style = questTextStyle(20f * scale, FontWeight.Medium),
                )
                Spacer(Modifier.height(46.dp * scale))
                Text(
                    stringResource(R.string.mission_judge_heading),
                    color = foreground,
                    style = questTextStyle(22f * scale, FontWeight.Bold),
                )
                Spacer(Modifier.height(8.dp * scale))
                Text(
                    stringResource(R.string.mission_judge_hint),
                    color = colorResource(R.color.quest_count_control),
                    style = questTextStyle(14f * scale, FontWeight.Normal),
                )
                Spacer(Modifier.height(33.dp * scale))
                Column(verticalArrangement = Arrangement.spacedBy(20.dp * scale)) {
                    partnerGuesses.forEachIndexed { index, guess ->
                        JudgementCard(
                            number = index + 1,
                            guess = guess,
                            isCorrect = index in correctIndices,
                            onToggle = { onToggle(index, it) },
                            onInspect = { onInspect(index) },
                            scale = scale,
                        )
                    }
                }
                Spacer(Modifier.weight(1f).heightIn(min = 32.dp * scale))
                Button(
                    onClick = onResult,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp * scale),
                    shape = RoundedCornerShape(8.dp * scale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.entrance_create_background),
                        contentColor = background,
                    ),
                ) {
                    Text(stringResource(R.string.mission_judge_result), style = questTextStyle(20f * scale, FontWeight.SemiBold))
                }
            }
        }
    }
}

@Composable
private fun JudgementCard(
    number: Int,
    guess: String,
    isCorrect: Boolean,
    onToggle: (Boolean) -> Unit,
    onInspect: () -> Unit,
    scale: Float,
) {
    val green = colorResource(R.color.quest_mode_apart_base)
    val highlight = colorResource(R.color.quest_mode_apart_light)
    val foreground = colorResource(R.color.quest_mode_card_text)
    val idle = colorResource(R.color.quest_selection_inactive)
    val fill = if (isCorrect) Brush.verticalGradient(
        0f to green, 0.69952f to green, 1f to highlight.copy(alpha = 0.6f).compositeOver(green),
    ) else Brush.verticalGradient(listOf(idle, idle))

    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 83.dp * scale)
            .clip(RoundedCornerShape(8.dp * scale))
            .background(fill)
            .toggleable(value = isCorrect, role = Role.Checkbox, onValueChange = onToggle)
            .padding(start = 16.dp * scale, top = 16.dp * scale, end = 16.dp * scale, bottom = 20.dp * scale),
        horizontalArrangement = Arrangement.spacedBy(12.dp * scale),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp * scale)) {
            Text(
                stringResource(R.string.mission_guess_field, number),
                color = if (isCorrect) foreground else green,
                style = questTextStyle(12f * scale, FontWeight.Medium),
                modifier = Modifier.clickable(
                    onClickLabel = stringResource(R.string.mission_judge_show_mission),
                    onClick = onInspect,
                ),
            )
            Text(
                guess.ifBlank { stringResource(R.string.mission_guess_empty_answer) },
                color = foreground,
                style = questTextStyle(16f * scale, FontWeight.Medium),
            )
        }
        Image(
            painterResource(if (isCorrect) R.drawable.mission_judge_selected else R.drawable.mission_judge_unselected),
            contentDescription = null,
            modifier = Modifier.size(24.dp * scale),
        )
    }
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun MissionJudgementPreview() {
    val sample = stringResource(R.string.mission_judge_show_mission)
    MissionJudgementContent(List(3) { sample }, listOf(0), { _, _ -> }, {}, {})
}
