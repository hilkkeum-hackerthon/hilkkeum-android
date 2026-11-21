package kr.co.cmc.hil.presentation.invitation

import android.content.ClipData
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.cmc.hil.R
import kotlinx.coroutines.launch

private val InvitationFont = FontFamily(
    Font(R.font.pretendard_regular, FontWeight.Normal),
    Font(R.font.pretendard_medium, FontWeight.Medium),
    Font(R.font.pretendard_semibold, FontWeight.SemiBold),
)

@Composable
fun InvitationScreen(
    onBack: () -> Unit,
    shareCode: String? = null,
    onJoin: ((String) -> Boolean)? = null,
) {
    var enteredCode by rememberSaveable { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    val clipboard = LocalClipboard.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copyLabel = stringResource(R.string.invitation_copy)
    val emptyMessage = stringResource(R.string.invitation_hint)
    val unavailable = stringResource(R.string.entrance_invite_unavailable)
    val invalid = stringResource(R.string.invitation_invalid_code)
    val submit: () -> Unit = {
        keyboard?.hide()
        val message = when {
            enteredCode.isBlank() -> emptyMessage
            onJoin == null -> unavailable
            onJoin(enteredCode) -> null
            else -> invalid
        }
        if (message != null) {
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                snackbar.showSnackbar(message)
            }
        }
    }

    Box(
        Modifier.fillMaxSize()
            .background(colorResource(R.color.entrance_background))
            .imePadding(),
    ) {
        InvitationContent(
            shareCode = shareCode,
            enteredCode = enteredCode,
            onCodeChange = { enteredCode = it },
            onBack = onBack,
            onSubmit = submit,
            onCopy = {
                if (shareCode != null) {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(copyLabel, shareCode)))
                    }
                }
            },
        )
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).offset(y = (-32).dp))
    }
}

@Composable
private fun InvitationContent(
    shareCode: String?,
    enteredCode: String,
    onCodeChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    onCopy: () -> Unit,
) {
    val sharing = shareCode != null
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        BoxWithConstraints(Modifier.widthIn(max = 480.dp).fillMaxWidth()) {
            val factor = maxWidth / 360.dp
            val controlShape = RoundedCornerShape(8.dp * factor)
            Box(
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .height(740.dp * factor),
            ) {
                InvitationArtwork(Modifier.fillMaxSize())
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.offset(x = 10.dp * factor, y = 28.dp * factor)
                        .size(maxOf(48.dp, 48.dp * factor)),
                ) {
                    Image(
                        painterResource(R.drawable.invitation_chevron),
                        stringResource(R.string.invitation_back),
                        Modifier.size(24.dp * factor),
                    )
                }
                Text(
                    text = stringResource(
                        if (sharing) R.string.invitation_share_header else R.string.invitation_join_header,
                    ),
                    style = invitationTextStyle(18 * factor, FontWeight.Medium),
                    color = colorResource(R.color.invitation_title),
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 96.dp * factor),
                )
                Text(
                    text = stringResource(
                        if (sharing) R.string.invitation_share_body else R.string.invitation_join_body,
                    ),
                    style = invitationTextStyle(18 * factor, FontWeight.SemiBold),
                    color = colorResource(R.color.invitation_title),
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 236.dp * factor),
                )

                val fieldModifier = Modifier.align(Alignment.TopCenter).offset(y = 329.dp * factor)
                    .width(260.dp * factor).heightIn(min = 46.dp * factor)
                    .clip(controlShape).background(colorResource(R.color.invitation_field))
                if (shareCode != null) {
                    val copyLabel = stringResource(R.string.invitation_copy)
                    Row(
                        modifier = fieldModifier.clickable(
                            role = Role.Button,
                            onClickLabel = copyLabel,
                            onClick = onCopy,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp * factor, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            shareCode,
                            style = invitationTextStyle(16 * factor, FontWeight.SemiBold),
                            color = colorResource(R.color.entrance_invite_text),
                        )
                        Image(painterResource(R.drawable.invitation_copy), null, Modifier.size(24.dp * factor))
                    }
                } else {
                    InviteCodeField(enteredCode, onCodeChange, onSubmit, factor, fieldModifier)
                    Button(
                        onClick = onSubmit,
                        modifier = Modifier.align(Alignment.TopCenter).offset(y = 571.dp * factor)
                            .width(328.dp * factor).heightIn(min = 52.dp * factor),
                        shape = controlShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(R.color.entrance_create_background),
                            contentColor = colorResource(R.color.entrance_create_text),
                        ),
                    ) {
                        Text(
                            stringResource(R.string.invitation_submit),
                            style = invitationTextStyle(20 * factor, FontWeight.SemiBold),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteCodeField(
    code: String,
    onCodeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    factor: Float,
    modifier: Modifier,
) {
    val hint = stringResource(R.string.invitation_hint)
    val textColor = colorResource(R.color.entrance_invite_text)
    val style = invitationTextStyle(16 * factor, FontWeight.Normal)
    BasicTextField(
        value = code,
        onValueChange = onCodeChange,
        modifier = modifier.semantics { contentDescription = hint },
        textStyle = style.copy(color = textColor),
        cursorBrush = SolidColor(textColor),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Characters,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        decorationBox = { innerField ->
            Box(Modifier.fillMaxWidth().heightIn(min = 46.dp * factor), contentAlignment = Alignment.Center) {
                if (code.isEmpty()) {
                    Text(hint, color = colorResource(R.color.invitation_hint), style = style)
                }
                innerField()
            }
        },
    )
}

private fun invitationTextStyle(size: Float, weight: FontWeight) = TextStyle(
    fontFamily = InvitationFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * 1.4f).sp,
    letterSpacing = (-size * 0.03f).sp,
    textAlign = TextAlign.Center,
)
