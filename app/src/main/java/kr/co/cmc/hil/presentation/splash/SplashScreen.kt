package kr.co.cmc.hil.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.common.homeFrameScale

/** 스플래시를 최소한 보여주는 시간. 방 세션 복원이 더 오래 걸리면 복원이 끝날 때까지 유지한다. */
internal const val SplashMinimumMillis = 1_200L

private val SplashFont = FontFamily(Font(R.font.pretendard_semibold, FontWeight.SemiBold))

// Figma Splash(269:6223): 화면 아래쪽 376dp에 주황색이 투명 → 약 8% 불투명도로 깔린다.
private const val GlowHeight = 376f
private const val GlowStartFraction = 0.4739f
private const val GlowEndAlpha = 0.082f

/**
 * 앱 시작 시 로고와 슬로건을 보여주는 스플래시 화면. 상태가 없는 화면이라 ViewModel 없이 둔다.
 * 최소 표시 시간이 지나면 [onFinished]를 한 번 호출한다.
 */
@Composable
internal fun SplashScreen(onFinished: () -> Unit) {
    val currentOnFinished by rememberUpdatedState(onFinished)

    LaunchedEffect(Unit) {
        delay(SplashMinimumMillis)
        currentOnFinished()
    }

    SplashContent()
}

@Composable
private fun SplashContent() {
    val glow = colorResource(R.color.entrance_create_background)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(colorResource(R.color.entrance_background)),
        contentAlignment = Alignment.Center,
    ) {
        val scale = homeFrameScale
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(GlowHeight.dp * scale)
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        GlowStartFraction to Color.Transparent,
                        1f to glow.copy(alpha = GlowEndAlpha),
                    ),
                ),
        )
        Box(Modifier.size(360.dp * scale, 740.dp * scale)) {
            Text(
                text = stringResource(R.string.splash_tagline),
                color = colorResource(R.color.entrance_subtitle_text),
                style = TextStyle(
                    fontFamily = SplashFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (14 * scale).sp,
                    lineHeight = (19.6 * scale).sp,
                    letterSpacing = (-0.42 * scale).sp,
                ),
                modifier = Modifier.align(Alignment.TopCenter).offset(x = 5.dp * scale, y = 254.dp * scale),
            )
            Image(
                painter = painterResource(R.drawable.splash_logo),
                contentDescription = stringResource(R.string.entrance_title),
                modifier = Modifier.offset(90.dp * scale, 284.dp * scale)
                    .size(189.265.dp * scale, 125.872.dp * scale),
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun SplashPreview() {
    SplashContent()
}
