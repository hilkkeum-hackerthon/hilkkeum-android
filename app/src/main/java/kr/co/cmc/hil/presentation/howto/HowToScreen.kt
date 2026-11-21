package kr.co.cmc.hil.presentation.howto

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.stamp.wishStyle

private data class HowToSection(
    @StringRes val title: Int,
    @StringRes val body: Int,
)

private val HowToSections = listOf(
    HowToSection(R.string.howto_mission_title, R.string.howto_mission_body),
    HowToSection(R.string.howto_observe_title, R.string.howto_observe_body),
    HowToSection(R.string.howto_guess_title, R.string.howto_guess_body),
    HowToSection(R.string.howto_score_title, R.string.howto_score_body),
    HowToSection(R.string.howto_penalty_title, R.string.howto_penalty_body),
    HowToSection(R.string.howto_stamp_title, R.string.howto_stamp_body),
)

/**
 * 게임 규칙을 보여주는 이용 방법 화면 (FR-053). 상태가 없는 화면이라 ViewModel 없이 Content만 둔다.
 */
@Composable
internal fun HowToScreen(onBack: () -> Unit) {
    HowToContent(onBack = onBack)
}

@Composable
private fun HowToContent(onBack: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(colorResource(R.color.entrance_background)),
        contentAlignment = Alignment.TopCenter,
    ) {
        BoxWithConstraints(Modifier.widthIn(max = 480.dp).fillMaxSize()) {
            val factor = maxWidth / 360.dp
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(top = 28.dp * factor, start = 10.dp * factor)
                        .heightIn(min = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(maxOf(48.dp, 48.dp * factor))) {
                        Image(
                            painterResource(R.drawable.invitation_chevron),
                            stringResource(R.string.howto_back),
                            Modifier.size(24.dp * factor),
                        )
                    }
                    Text(
                        stringResource(R.string.howto_title),
                        color = Color.White,
                        style = wishStyle(20f * factor, FontWeight.Medium),
                        modifier = Modifier.semantics { heading() },
                    )
                }
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp * factor, vertical = 12.dp * factor),
                    verticalArrangement = Arrangement.spacedBy(12.dp * factor),
                ) {
                    Text(
                        stringResource(R.string.howto_intro),
                        color = colorResource(R.color.wish_coupon_value),
                        style = wishStyle(14f * factor),
                    )
                    HowToSections.forEach { section ->
                        HowToSectionCard(section, factor)
                    }
                }
            }
        }
    }
}

@Composable
private fun HowToSectionCard(section: HowToSection, factor: Float) {
    Column(
        Modifier.fillMaxWidth()
            .background(colorResource(R.color.home_help_background), RoundedCornerShape(8.dp * factor))
            .padding(12.dp * factor),
        verticalArrangement = Arrangement.spacedBy(6.dp * factor),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp * factor),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(R.drawable.home_help_bullet),
                contentDescription = null,
                modifier = Modifier.size(19.dp * factor),
            )
            Text(
                stringResource(section.title),
                color = colorResource(R.color.entrance_create_background),
                style = wishStyle(16f * factor, FontWeight.SemiBold),
                modifier = Modifier.semantics { heading() },
            )
        }
        Text(
            stringResource(section.body),
            color = Color.White,
            style = wishStyle(14f * factor),
        )
    }
}

@Preview(widthDp = 360, heightDp = 740)
@Composable
private fun HowToPreview() {
    HowToContent(onBack = {})
}
