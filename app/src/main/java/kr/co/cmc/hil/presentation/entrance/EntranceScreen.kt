package kr.co.cmc.hil.presentation.entrance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.ui.theme.HilkkeumTheme

private val EntranceFont = FontFamily(Font(R.font.pretendard_semibold, FontWeight.SemiBold))
private val ReferenceWidth = 360.dp
private val MaximumContentWidth = 480.dp
private val MinimumContentHeight = 700.dp
private val ButtonShape = RoundedCornerShape(8.dp)

@Composable
fun EntranceScreen(onEnterInviteCode: () -> Unit, onCreateRoom: () -> Unit) {
    EntranceContent(onEnterInviteCode, onCreateRoom)
}

@Composable
private fun EntranceContent(
    onEnterInviteCode: () -> Unit,
    onCreateRoom: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradientEnd = colorResource(R.color.entrance_gradient_bottom)
    val background = Brush.verticalGradient(
        0.018506f to colorResource(R.color.entrance_gradient_top),
        0.54905f to gradientEnd,
        1f to gradientEnd,
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.entrance_background))
            .background(background),
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
            EntranceArtwork(artScale)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 76.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                val inviteText = colorResource(R.color.entrance_invite_text)
                EntranceButton(
                    label = stringResource(R.string.entrance_invite),
                    onClick = onEnterInviteCode,
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
    Text(
        text = stringResource(R.string.entrance_subtitle),
        color = colorResource(R.color.entrance_subtitle_text),
        fontFamily = EntranceFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.6.sp,
        letterSpacing = (-0.42).sp,
        modifier = Modifier.align(Alignment.TopCenter).offset(y = 100.dp * scale),
    )
    Image(
        painter = painterResource(R.drawable.entrance_wordmark),
        contentDescription = stringResource(R.string.entrance_title),
        modifier = Modifier.align(Alignment.TopCenter)
            .offset(x = 0.5.dp * scale, y = 130.dp * scale)
            .size(110.dp * scale, 56.dp * scale),
    )

    EntranceGraphic(R.drawable.entrance_cord, -11f, 292.96f, 3f, 92.5f, scale)
    EntranceGraphic(R.drawable.entrance_cord, 11f, 292.96f, 3f, 92.5f, scale)
    EntranceGraphic(R.drawable.entrance_tail_shadow, 8.93f, 384.86f, 20.92f, 63.5f, scale, 4.47f)
    EntranceGraphic(R.drawable.entrance_tail, 12.7f, 380.96f, 18.6f, 63f, scale)
    EntranceGraphic(R.drawable.entrance_eyelet, 0f, 266.46f, 56f, 56f, scale)
    EntranceGraphic(R.drawable.entrance_eyelet, 0f, 348.46f, 56f, 56f, scale)
}

@Composable
private fun BoxScope.EntranceGraphic(
    drawable: Int,
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
        Text(
            text = label,
            fontFamily = EntranceFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.6).sp,
        )
    }
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun EntrancePreview() {
    HilkkeumTheme {
        EntranceContent(onEnterInviteCode = {}, onCreateRoom = {})
    }
}
