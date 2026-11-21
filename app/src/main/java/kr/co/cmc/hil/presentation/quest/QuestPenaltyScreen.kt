package kr.co.cmc.hil.presentation.quest

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.ui.theme.HilkkeumTheme

private data class PenaltyCard(
    val id: Int,
    val centerX: Float,
    val centerY: Float,
    val rotation: Float,
    @field:ColorRes val color: Int,
    @field:DrawableRes val pin: Int,
)

private val PenaltyCards = listOf(
    PenaltyCard(1, 98.36f, 242.38f, -8.93f, R.color.quest_penalty_green,
        R.drawable.quest_penalty_pin_orange),
    PenaltyCard(2, 261.02f, 261.54f, 6.39f, R.color.quest_penalty_purple,
        R.drawable.quest_penalty_pin_yellow),
    PenaltyCard(3, 184.09f, 377.69f, 4.78f, R.color.quest_penalty_yellow,
        R.drawable.quest_penalty_pin_blue),
    PenaltyCard(4, 89.98f, 488.79f, 15.03f, R.color.quest_penalty_blue,
        R.drawable.quest_penalty_pin_yellow),
    PenaltyCard(5, 262.50f, 522.68f, -9.2f, R.color.quest_penalty_gray,
        R.drawable.quest_penalty_pin_white),
)

@Composable
fun QuestPenaltyScreen(onNext: (Int) -> Unit) {
    var selectedId by rememberSaveable { mutableStateOf<Int?>(null) }

    QuestPenaltyContent(
        selectedId = selectedId,
        onSelect = { id -> selectedId = id.takeUnless { it == selectedId } },
        onNext = { selectedId?.let(onNext) },
    )
}

@Composable
private fun QuestPenaltyContent(
    selectedId: Int?,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
) {
    val background = colorResource(R.color.entrance_background)
    val selected = PenaltyCards.firstOrNull { it.id == selectedId }
    val hasSelection = selected != null

    Box(
        modifier = Modifier.fillMaxSize().background(background),
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
                    painter = painterResource(R.drawable.quest_selection_progress),
                    contentDescription = stringResource(R.string.quest_selection_progress),
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(y = 92.dp * scale).size(40.dp * scale, 8.dp * scale),
                )
                Text(
                    text = stringResource(R.string.quest_penalty_title),
                    color = colorResource(R.color.quest_mode_title),
                    style = questTextStyle(22f * scale, FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 120.dp * scale)
                        .padding(horizontal = 16.dp * scale),
                )
                Box(Modifier.matchParentSize().selectableGroup()) {
                    PenaltyArtwork(R.drawable.quest_penalty_path_selected, 96.33f, 211.51f, 168.26f, 283.95f, scale)

                    PenaltyCards.forEach { card ->
                        PenaltyOption(
                            card = card,
                            isSelected = card.id == selectedId,
                            scale = scale,
                            onClick = { onSelect(card.id) },
                        )
                    }
                    if (selected != null) {
                        PenaltyArtwork(
                            R.drawable.quest_penalty_ring,
                            selected.centerX - 104.13f,
                            selected.centerY - 60.15f,
                            200.581f, 121.91f, scale,
                        )
                    }
                }
                Button(
                    onClick = onNext,
                    enabled = hasSelection,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .offset(x = 4.dp * scale).padding(bottom = 78.dp * scale)
                        .width(328.dp * scale).heightIn(min = maxOf(48.dp, 52.dp * scale)),
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
private fun BoxScope.PenaltyOption(
    card: PenaltyCard,
    isSelected: Boolean,
    scale: Float,
    onClick: () -> Unit,
) {
    val description = stringResource(R.string.quest_penalty_option, card.id)
    Column(
        modifier = Modifier.align(Alignment.TopStart)
            .offset((card.centerX - 70f).dp * scale, (card.centerY - 40f).dp * scale)
            .size(140.dp * scale, 80.dp * scale)
            .rotate(card.rotation)
            .clip(RoundedCornerShape(8.dp * scale))
            .background(colorResource(card.color))
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(start = 10.dp * scale, end = 10.dp * scale, top = 4.dp * scale, bottom = 10.dp * scale),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp * scale, Alignment.CenterVertically),
    ) {
        Image(painterResource(card.pin), contentDescription = null, modifier = Modifier.size(8.dp * scale))
        Text(
            text = stringResource(R.string.quest_penalty_sample),
            color = colorResource(R.color.quest_mode_card_text),
            style = questTextStyle(16f * scale, FontWeight.Medium),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PenaltyArtwork(
    @DrawableRes image: Int,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
    scale: Float,
) {
    Image(
        painter = painterResource(image),
        contentDescription = null,
        modifier = Modifier.offset(left.dp * scale, top.dp * scale)
            .size(width.dp * scale, height.dp * scale),
    )
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun QuestPenaltyPreview() {
    HilkkeumTheme { QuestPenaltyContent(selectedId = 3, onSelect = {}, onNext = {}) }
}
