package kr.co.cmc.hil.presentation.quest

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.ui.theme.HilkkeumTheme


@Composable
fun QuestSelectionScreen(
    missionCount: Int,
    labels: List<String>,
    isSubmitting: Boolean,
    catalogStatus: (@Composable () -> Unit)? = null,
    onNext: (List<Int>) -> Unit,
) {
    var selected by rememberSaveable(missionCount, labels) { mutableStateOf(arrayListOf<Int>()) }

    QuestSelectionContent(
        missionCount = missionCount,
        selected = selected,
        labels = labels,
        isSubmitting = isSubmitting,
        catalogStatus = catalogStatus,
        onToggle = { id ->
            if (id in selected) {
                selected = ArrayList(selected - id)
            } else if (selected.size < missionCount) {
                selected = ArrayList(selected + id)
            }
        },
        onNext = { onNext(selected.toList()) },
    )
}

@Composable
private fun QuestSelectionContent(
    missionCount: Int,
    selected: List<Int>,
    labels: List<String>,
    isSubmitting: Boolean = false,
    catalogStatus: (@Composable () -> Unit)? = null,
    onToggle: (Int) -> Unit,
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

            Column(
                modifier = Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = contentHeight)
                    .padding(top = 92.dp * scale, bottom = 78.dp * scale),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.quest_selection_progress),
                    contentDescription = stringResource(R.string.quest_selection_progress),
                    modifier = Modifier.size(40.dp * scale, 8.dp * scale),
                )
                Spacer(Modifier.height(23.dp * scale))
                Text(
                    text = stringResource(R.string.quest_selection_title),
                    color = colorResource(R.color.quest_mode_title),
                    style = questTextStyle(22f * scale, FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.offset(x = 3.5.dp * scale).padding(horizontal = 16.dp * scale),
                )
                Text(
                    text = stringResource(R.string.quest_selection_required, missionCount),
                    color = colorResource(R.color.quest_count_control),
                    style = questTextStyle(14f * scale, FontWeight.Normal),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(32.dp * scale))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp * scale),
                    verticalArrangement = Arrangement.spacedBy(8.dp * scale),
                ) {
                    labels.forEachIndexed { id, label ->
                        QuestSelectionRow(
                            label = label,
                            isSelected = id in selected,
                            canToggle = id in selected || selected.size < missionCount,
                            scale = scale,
                            onToggle = { onToggle(id) },
                        )
                    }
                }
                catalogStatus?.invoke()
                Spacer(Modifier.heightIn(min = 48.dp * scale).weight(1f))
                Button(
                    onClick = onNext,
                    enabled = selected.size == missionCount && !isSubmitting && catalogStatus == null,
                    modifier = Modifier.offset(x = 4.dp * scale)
                        .width(328.dp * scale)
                        .heightIn(min = maxOf(48.dp, 52.dp * scale)),
                    shape = RoundedCornerShape(8.dp * scale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.entrance_create_background),
                        contentColor = background,
                    ),
                ) {
                    Text(stringResource(R.string.quest_mode_next), style = questTextStyle(20f * scale, FontWeight.SemiBold))
                }
            }
        }
    }
}

@Composable
private fun QuestSelectionRow(
    label: String,
    isSelected: Boolean,
    canToggle: Boolean,
    scale: Float,
    onToggle: () -> Unit,
) {
    val rowColor = if (isSelected) R.color.quest_mode_together_base else R.color.quest_selection_inactive
    val icon = if (isSelected) R.drawable.quest_selection_selected else R.drawable.quest_selection_unselected
    Row(
        modifier = Modifier.fillMaxWidth()
            .heightIn(min = 44.dp * scale)
            .clip(RoundedCornerShape(4.dp * scale))
            .background(colorResource(rowColor))
            .toggleable(value = isSelected, enabled = canToggle, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 12.dp * scale, vertical = 10.dp * scale),
        horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp * scale))
        Text(
            text = label,
            color = colorResource(R.color.quest_selection_text),
            style = questTextStyle(16f * scale, FontWeight.Normal),
        )
    }
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun QuestSelectionPreview() {
    HilkkeumTheme {
        QuestSelectionContent(3, listOf(2, 4), List(7) { stringResource(R.string.quest_selection_sample, it + 1) }, onToggle = {}, onNext = {})
    }
}
