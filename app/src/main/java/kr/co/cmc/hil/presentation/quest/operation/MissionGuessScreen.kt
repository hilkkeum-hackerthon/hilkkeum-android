package kr.co.cmc.hil.presentation.quest.operation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.quest.questTextStyle

private const val MaxGuessCharacters = 30

@Composable
internal fun MissionGuessScreen(
    missionCount: Int,
    submittedAnswers: List<String>?,
    isSubmitting: Boolean = false,
    onSubmit: (List<String>) -> Unit,
) {
    var answers by rememberSaveable(missionCount) {
        mutableStateOf(ArrayList(List(missionCount) { submittedAnswers?.getOrNull(it).orEmpty() }))
    }
    val isSubmitted = submittedAnswers != null
    val canSubmit = !isSubmitted && !isSubmitting && answers.isNotEmpty() && answers.all(String::isNotEmpty)
    val focus = LocalFocusManager.current

    MissionGuessContent(
        answers = answers,
        isSubmitted = isSubmitted,
        canSubmit = canSubmit,
        isSubmitting = isSubmitting,
        onAnswerChange = { index, value ->
            answers = ArrayList(answers).apply { set(index, value) }
        },
        onSubmit = {
            if (canSubmit) {
                focus.clearFocus()
                onSubmit(answers.toList())
            }
        },
    )
}

@Composable
private fun MissionGuessContent(
    answers: List<String>,
    isSubmitted: Boolean,
    canSubmit: Boolean = !isSubmitted && answers.isNotEmpty() && answers.all(String::isNotEmpty),
    isSubmitting: Boolean = false,
    onAnswerChange: (Int, String) -> Unit,
    onSubmit: () -> Unit,
) {
    val background = colorResource(R.color.entrance_background)
    val foreground = colorResource(R.color.quest_mode_card_text)
    val muted = colorResource(R.color.quest_count_control)
    val orange = colorResource(R.color.entrance_create_background)

    Box(Modifier.fillMaxSize().background(background), contentAlignment = Alignment.TopCenter) {
        BoxWithConstraints(Modifier.widthIn(max = 480.dp).fillMaxSize().navigationBarsPadding().imePadding()) {
            val scale = maxWidth / 360.dp
            val viewportHeight = maxHeight

            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = viewportHeight)
                    .padding(start = 16.dp * scale, end = 16.dp * scale, top = 38.dp * scale, bottom = 36.dp * scale),
            ) {
                Text(
                    stringResource(R.string.mission_guess_title),
                    color = foreground,
                    style = questTextStyle(20f * scale, FontWeight.Medium),
                )
                Spacer(Modifier.height(46.dp * scale))
                Text(
                    stringResource(R.string.mission_guess_heading),
                    color = foreground,
                    style = questTextStyle(22f * scale, FontWeight.Bold),
                )
                Spacer(Modifier.height(8.dp * scale))
                Text(
                    stringResource(R.string.mission_guess_readonly),
                    color = muted,
                    style = questTextStyle(14f * scale, FontWeight.Normal),
                )
                Spacer(Modifier.height(62.dp * scale))
                Column(verticalArrangement = Arrangement.spacedBy(20.dp * scale)) {
                    answers.forEachIndexed { index, answer ->
                        MissionGuessField(
                            number = index + 1,
                            value = answer,
                            readOnly = isSubmitted || isSubmitting,
                            onChange = { onAnswerChange(index, it) },
                            scale = scale,
                        )
                    }
                }
                Spacer(Modifier.heightIn(min = 32.dp * scale).weight(1f))
                Button(
                    onClick = onSubmit,
                    enabled = canSubmit,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp * scale),
                    shape = RoundedCornerShape(8.dp * scale),
                    colors = ButtonDefaults.buttonColors(containerColor = orange, contentColor = background),
                ) {
                    Text(
                        stringResource(if (isSubmitted) R.string.mission_guess_submitted else R.string.mission_guess_submit),
                        style = questTextStyle(20f * scale, FontWeight.SemiBold),
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionGuessField(
    number: Int,
    value: String,
    readOnly: Boolean,
    onChange: (String) -> Unit,
    scale: Float,
) {
    val label = stringResource(R.string.mission_guess_field, number)
    val foreground = colorResource(R.color.quest_mode_card_text)
    val accent = colorResource(R.color.quest_mode_apart_base)
    val style = questTextStyle(16f * scale, FontWeight.Medium)

    Column(
        Modifier.fillMaxWidth().heightIn(min = 83.dp * scale)
            .background(colorResource(R.color.quest_selection_inactive), RoundedCornerShape(8.dp * scale))
            .padding(horizontal = 16.dp * scale, vertical = 14.dp * scale),
        verticalArrangement = Arrangement.spacedBy(7.dp * scale, Alignment.CenterVertically),
    ) {
        Text(label, color = accent, style = questTextStyle(12f * scale, FontWeight.Medium))
        BasicTextField(
            value = value,
            onValueChange = {
                if (it.codePointCount(0, it.length) <= MaxGuessCharacters) onChange(it)
            },
            readOnly = readOnly,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            cursorBrush = SolidColor(accent),
            textStyle = style.copy(color = foreground),
            decorationBox = { content ->
                Box {
                    if (value.isEmpty()) {
                        Text(
                            stringResource(if (readOnly) R.string.mission_guess_empty_answer else R.string.mission_guess_hint),
                            color = colorResource(R.color.quest_count_control),
                            style = style,
                        )
                    }
                    content()
                }
            },
        )
    }
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun MissionGuessPreview() {
    MissionGuessContent(listOf("", "", ""), false, onAnswerChange = { _, _ -> }, onSubmit = {})
}
