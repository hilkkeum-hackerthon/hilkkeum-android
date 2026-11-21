package kr.co.cmc.hil.presentation.entrance

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kr.co.cmc.hil.R
import kr.co.cmc.hil.ui.theme.HilkkeumTheme

private val EntranceFont = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
)
private val ReferenceWidth = 360.dp
private val MaximumContentWidth = 480.dp
private val MinimumContentHeight = 700.dp
private val ButtonShape = RoundedCornerShape(8.dp)

// Figma basic frame 21:1245: 카드가 열리면 고리·줄·꼬리가 34.46dp 위로 올라간다.
private const val OpenArtworkShift = 34.46f

/**
 * 첫 화면. '초대 코드 입력하기'를 누르면 같은 화면 위에 초대 코드 입력 카드가 열린다 (Figma 21:1245).
 * 카드 표시 여부와 입력 중인 코드는 화면 전용 UI 상태라 Screen이 소유한다.
 */
@Composable
fun EntranceScreen(
    onCreateRoom: () -> Unit,
    onJoin: (String) -> Unit,
    isSubmitting: Boolean = false,
) {
    var isInviteOpen by rememberSaveable { mutableStateOf(false) }
    var inviteCode by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val emptyMessage = stringResource(R.string.invitation_hint)
    val submit: () -> Unit = {
        if (isSubmitting) {
            // 입장 요청이 진행 중이다.
        } else if (inviteCode.isBlank()) {
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(emptyMessage)
            }
        } else {
            keyboard?.hide()
            onJoin(inviteCode.trim())
        }
    }

    BackHandler(enabled = isInviteOpen) {
        keyboard?.hide()
        isInviteOpen = false
    }

    Box(Modifier.fillMaxSize()) {
        EntranceContent(
            isInviteOpen = isInviteOpen,
            inviteCode = inviteCode,
            isSubmitting = isSubmitting,
            onOpenInvite = { isInviteOpen = true },
            onInviteCodeChange = { inviteCode = it },
            onSubmitInvite = submit,
            onCreateRoom = {
                keyboard?.hide()
                isInviteOpen = false
                onCreateRoom()
            },
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp))
    }
}

@Composable
private fun EntranceContent(
    isInviteOpen: Boolean,
    inviteCode: String,
    isSubmitting: Boolean,
    onOpenInvite: () -> Unit,
    onInviteCodeChange: (String) -> Unit,
    onSubmitInvite: () -> Unit,
    onCreateRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradientEnd = colorResource(R.color.entrance_gradient_bottom)
    val background = if (isInviteOpen) {
        Brush.verticalGradient(
            0.175f to colorResource(R.color.entrance_open_gradient_top),
            1f to gradientEnd,
        )
    } else {
        Brush.verticalGradient(
            0.018506f to colorResource(R.color.entrance_gradient_top),
            0.54905f to gradientEnd,
            1f to gradientEnd,
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.entrance_background))
            .background(background)
            .imePadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val contentHeight = maxOf(maxHeight, MinimumContentHeight)
        val artScale = minOf(maxWidth / ReferenceWidth, 1f)
        Box(
            modifier = Modifier
                .widthIn(max = MaximumContentWidth)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .height(contentHeight),
        ) {
            if (isInviteOpen) {
                OpenEntranceArtwork(artScale)
                InviteCodeCard(
                    code = inviteCode,
                    isSubmitting = isSubmitting,
                    onCodeChange = onInviteCodeChange,
                    onSubmit = onSubmitInvite,
                    modifier = Modifier.offset(y = 243.dp * artScale)
                        .padding(horizontal = 16.dp).fillMaxWidth(),
                )
            } else {
                EntranceArtwork(artScale)
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 76.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val inviteText = colorResource(
                    if (isInviteOpen) R.color.entrance_open_invite_text else R.color.entrance_invite_text,
                )
                EntranceButton(
                    label = stringResource(R.string.entrance_invite),
                    onClick = onOpenInvite,
                    background = colorResource(R.color.entrance_invite_background),
                    foreground = inviteText,
                    border = BorderStroke(1.dp, inviteText),
                )
                EntranceButton(
                    label = stringResource(R.string.entrance_create_room),
                    onClick = onCreateRoom,
                    background = colorResource(R.color.entrance_create_background),
                    foreground = colorResource(R.color.entrance_create_text),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.EntranceArtwork(scale: Float) {
    Image(
        painter = painterResource(R.drawable.entrance_panel),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth().height(304.dp * scale),
        contentScale = ContentScale.FillBounds,
    )
    EntranceTitle(R.color.entrance_subtitle_text, FontWeight.SemiBold, scale)

    EntranceGraphic(R.drawable.entrance_cord, -11f, 292.96f, 3f, 92.5f, scale)
    EntranceGraphic(R.drawable.entrance_cord, 11f, 292.96f, 3f, 92.5f, scale)
    EntranceGraphic(R.drawable.entrance_tail_shadow, 8.93f, 384.86f, 20.92f, 63.5f, scale, 4.47f)
    EntranceGraphic(R.drawable.entrance_tail, 12.7f, 380.96f, 18.6f, 63f, scale)
    EntranceGraphic(R.drawable.entrance_eyelet, 0f, 266.46f, 56f, 56f, scale)
    EntranceGraphic(R.drawable.entrance_eyelet, 0f, 348.46f, 56f, 56f, scale)
}

@Composable
private fun BoxScope.OpenEntranceArtwork(scale: Float) {
    val panelShape = RoundedCornerShape(bottomStart = 40.dp * scale, bottomEnd = 40.dp * scale)

    Box(
        Modifier.fillMaxWidth().height(304.dp * scale)
            .background(colorResource(R.color.entrance_background), panelShape)
            .background(
                Brush.verticalGradient(
                    0f to colorResource(R.color.entrance_gradient_bottom),
                    1f to colorResource(R.color.entrance_open_panel_bottom),
                ),
                panelShape,
            ),
    )
    EntranceTitle(R.color.entrance_open_subtitle_text, FontWeight.Normal, scale)

    EntranceGraphic(R.drawable.entrance_cord_active, -11f, 292.96f - OpenArtworkShift, 3f, 92.5f, scale)
    EntranceGraphic(R.drawable.entrance_cord_active, 11f, 292.96f - OpenArtworkShift, 3f, 92.5f, scale)
    EntranceGraphic(R.drawable.entrance_tail_shadow, 8.93f, 384.86f - OpenArtworkShift, 20.92f, 63.5f, scale, 4.47f)
    EntranceGraphic(R.drawable.entrance_tail_active, 12.7f, 380.96f - OpenArtworkShift, 18.6025f, 63.0006f, scale)
    EntranceGraphic(R.drawable.entrance_eyelet_active, 0f, 238f, 36f, 36f, scale)
    EntranceGraphic(R.drawable.entrance_eyelet_active, 0f, 320f, 36f, 36f, scale)
}

@Composable
private fun BoxScope.EntranceTitle(subtitleColor: Int, subtitleWeight: FontWeight, scale: Float) {
    Text(
        text = stringResource(R.string.entrance_subtitle),
        color = colorResource(subtitleColor),
        style = entranceTextStyle(14f, subtitleWeight),
        modifier = Modifier.align(Alignment.TopCenter).offset(y = 100.dp * scale),
    )
    Image(
        painter = painterResource(R.drawable.entrance_wordmark),
        contentDescription = stringResource(R.string.entrance_title),
        modifier = Modifier.align(Alignment.TopCenter)
            .offset(x = 0.5.dp * scale, y = 130.dp * scale)
            .size(110.dp * scale, 56.dp * scale),
    )
}

@Composable
private fun InviteCodeCard(
    code: String,
    isSubmitting: Boolean,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fieldShape = RoundedCornerShape(8.dp)
    val hint = stringResource(R.string.invitation_hint)
    val fieldStyle = entranceTextStyle(16f, FontWeight.Normal)
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .background(colorResource(R.color.entrance_card_background), RoundedCornerShape(16.dp))
            .padding(start = 22.dp, end = 22.dp, top = 30.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.entrance_invite_card_title),
            color = Color.White,
            style = entranceTextStyle(18f, FontWeight.Medium),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BasicTextField(
                value = code,
                onValueChange = onCodeChange,
                modifier = Modifier.weight(1f).heightIn(min = 46.dp)
                    .background(colorResource(R.color.entrance_card_field), fieldShape)
                    .focusRequester(focusRequester)
                    .semantics { contentDescription = hint },
                textStyle = fieldStyle.copy(color = Color.White, textAlign = TextAlign.Start),
                cursorBrush = SolidColor(Color.White),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                decorationBox = { innerField ->
                    Box(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (code.isEmpty()) {
                            Text(hint, color = colorResource(R.color.entrance_card_hint), style = fieldStyle)
                        }
                        innerField()
                    }
                },
            )
            Button(
                onClick = onSubmit,
                enabled = !isSubmitting,
                modifier = Modifier.heightIn(min = 46.dp),
                shape = fieldShape,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.entrance_create_background),
                    contentColor = colorResource(R.color.entrance_create_text),
                    disabledContainerColor = colorResource(R.color.entrance_create_background),
                    disabledContentColor = colorResource(R.color.entrance_create_text),
                ),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = colorResource(R.color.entrance_create_text),
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Text(stringResource(R.string.entrance_invite_confirm), style = fieldStyle)
                }
            }
        }
    }
}

@Composable
private fun BoxScope.EntranceGraphic(
    @DrawableRes drawable: Int,
    centerOffset: Float,
    top: Float,
    width: Float,
    height: Float,
    scale: Float,
    rotation: Float = 0f,
) {
    Image(
        painter = painterResource(drawable),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(x = (centerOffset * scale).dp, y = (top * scale).dp)
            .size((width * scale).dp, (height * scale).dp)
            .rotate(rotation),
    )
}

@Composable
private fun EntranceButton(
    label: String,
    onClick: () -> Unit,
    background: Color,
    foreground: Color,
    border: BorderStroke? = null,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = ButtonShape,
        border = border,
        colors = ButtonDefaults.buttonColors(containerColor = background, contentColor = foreground),
    ) {
        Text(text = label, style = entranceTextStyle(20f, FontWeight.SemiBold))
    }
}

private fun entranceTextStyle(size: Float, weight: FontWeight) = TextStyle(
    fontFamily = EntranceFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * 1.4f).sp,
    letterSpacing = (-size * 0.03f).sp,
    textAlign = TextAlign.Center,
)

@Preview(widthDp = 360, heightDp = 740, name = "Closed")
@Composable
private fun EntrancePreview() {
    HilkkeumTheme {
        EntranceContent(false, "", false, {}, {}, {}, {})
    }
}

@Preview(widthDp = 360, heightDp = 740, name = "Invite card open")
@Composable
private fun EntranceInviteOpenPreview() {
    HilkkeumTheme {
        EntranceContent(true, "", false, {}, {}, {}, {})
    }
}
