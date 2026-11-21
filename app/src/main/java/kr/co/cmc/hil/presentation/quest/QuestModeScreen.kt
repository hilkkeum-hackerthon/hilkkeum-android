package kr.co.cmc.hil.presentation.quest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.ui.theme.HilkkeumTheme
import kotlinx.coroutines.launch

private val QuestFont = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
    Font(R.font.pretendard_bold, FontWeight.Bold),
)

@Composable
fun QuestModeScreen(onNext: (QuestMode) -> Unit) {
    var selection by rememberSaveable { mutableStateOf<QuestMode?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val chooseMessage = stringResource(R.string.quest_mode_choose)

    Box(Modifier.fillMaxSize()) {
        QuestModeContent(
            selection = selection,
            onSelect = { selection = it },
            onNext = {
                val mode = selection
                if (mode == null) {
                    if (snackbar.currentSnackbarData == null) {
                        scope.launch { snackbar.showSnackbar(chooseMessage) }
                    }
                } else {
                    onNext(mode)
                }
            },
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp))
    }
}

@Composable
private fun QuestModeContent(
    selection: QuestMode?,
    onSelect: (QuestMode) -> Unit,
    onNext: () -> Unit,
) {
    val background = colorResource(R.color.entrance_background)
    Box(
        modifier = Modifier.fillMaxSize().background(background),
        contentAlignment = Alignment.TopCenter,
    ) {
        BoxWithConstraints(Modifier.widthIn(max = 480.dp).fillMaxSize()) {
            val scale = maxWidth / 360.dp
            val contentHeight = maxOf(maxHeight, 740.dp * scale)
            Box(
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = contentHeight),
            ) {
                Image(
                    painter = painterResource(R.drawable.quest_mode_progress),
                    contentDescription = stringResource(R.string.quest_mode_progress),
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(y = 92.dp * scale)
                        .size(40.dp * scale, 8.dp * scale),
                )
                Text(
                    text = stringResource(R.string.quest_mode_title),
                    color = colorResource(R.color.quest_mode_title),
                    style = questTextStyle(22f * scale, FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.TopCenter)
                        .offset(y = 120.dp * scale)
                        .padding(horizontal = 16.dp * scale),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().offset(y = 259.dp * scale)
                        .padding(horizontal = 16.dp * scale)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp * scale),
                ) {
                    QuestModeOption(
                        label = stringResource(R.string.quest_mode_together),
                        base = colorResource(R.color.quest_mode_together_base),
                        light = colorResource(R.color.quest_mode_together_light),
                        selected = selection == QuestMode.Together,
                        dimmed = selection == QuestMode.Apart,
                        scale = scale,
                        onSelect = { onSelect(QuestMode.Together) },
                        modifier = Modifier.weight(1f),
                    )
                    QuestModeOption(
                        label = stringResource(R.string.quest_mode_apart),
                        base = colorResource(R.color.quest_mode_apart_base),
                        light = colorResource(R.color.quest_mode_apart_light),
                        selected = selection == QuestMode.Apart,
                        dimmed = selection == QuestMode.Together,
                        scale = scale,
                        onSelect = { onSelect(QuestMode.Apart) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Button(
                    onClick = onNext,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 16.dp * scale, end = 16.dp * scale, bottom = 76.dp * scale)
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
private fun QuestModeOption(
    label: String,
    base: Color,
    light: Color,
    selected: Boolean,
    dimmed: Boolean,
    scale: Float,
    onSelect: () -> Unit,
    modifier: Modifier,
) {
    val gradient = Brush.verticalGradient(
        0f to base,
        0.5f to base,
        1f to light.copy(alpha = 0.6f).compositeOver(base),
    )
    Box(
        modifier = modifier
            .heightIn(min = 222.dp * scale)
            .alpha(if (dimmed) 0.5f else 1f)
            .clip(RoundedCornerShape(16.dp * scale))
            .background(gradient)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 15.dp * scale, vertical = 20.dp * scale),
    ) {
        Text(
            text = label,
            color = colorResource(R.color.quest_mode_card_text),
            style = questTextStyle(18f * scale, FontWeight.SemiBold),
        )
    }
}

internal fun questTextStyle(size: Float, weight: FontWeight) = TextStyle(
    fontFamily = QuestFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * 1.4f).sp,
    letterSpacing = (-size * 0.03f).sp,
)

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun QuestModePreview() {
    HilkkeumTheme { QuestModeScreen(onNext = {}) }
}
