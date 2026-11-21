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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    shareCode: String,
    waitingMessage: String? = null,
) {
    val clipboard = LocalClipboard.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copyLabel = stringResource(R.string.invitation_copy)
    val copiedMessage = stringResource(R.string.invitation_copied)

    Box(
        Modifier.fillMaxSize()
            .background(colorResource(R.color.entrance_background)),
    ) {
        InvitationContent(
            shareCode = shareCode,
            onBack = onBack,
            onCopy = {
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(copyLabel, shareCode)))
                    snackbar.currentSnackbarData?.dismiss()
                    snackbar.showSnackbar(copiedMessage)
                }
            },
        )
        if (waitingMessage != null) {
            Text(
                text = waitingMessage,
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-64).dp),
            )
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).offset(y = (-32).dp))
    }
}

@Composable
private fun InvitationContent(
    shareCode: String,
    onBack: () -> Unit,
    onCopy: () -> Unit,
) {
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
                    text = stringResource(R.string.invitation_share_header),
                    style = invitationTextStyle(18 * factor, FontWeight.Medium),
                    color = colorResource(R.color.invitation_title),
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 96.dp * factor),
                )
                Text(
                    text = stringResource(R.string.invitation_share_body),
                    style = invitationTextStyle(18 * factor, FontWeight.SemiBold),
                    color = colorResource(R.color.invitation_title),
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 236.dp * factor),
                )

                val fieldModifier = Modifier.align(Alignment.TopCenter).offset(y = 329.dp * factor)
                    .width(260.dp * factor).heightIn(min = 46.dp * factor)
                    .clip(controlShape).background(colorResource(R.color.invitation_field))
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
            }
        }
    }
}

private fun invitationTextStyle(size: Float, weight: FontWeight) = TextStyle(
    fontFamily = InvitationFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * 1.4f).sp,
    letterSpacing = (-size * 0.03f).sp,
    textAlign = TextAlign.Center,
)
