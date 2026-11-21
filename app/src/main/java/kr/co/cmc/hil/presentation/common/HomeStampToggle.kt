package kr.co.cmc.hil.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kr.co.cmc.hil.R

private val TabFont = FontFamily(Font(R.font.pretendard_medium, FontWeight.Medium))

internal val BoxWithConstraintsScope.homeFrameScale: Float
    get() = minOf(maxWidth.coerceAtMost(480.dp) / 360.dp, maxHeight / 740.dp)

@Composable
internal fun BoxScope.HomeStampToggle(
    stampSelected: Boolean,
    onHome: () -> Unit,
    onStamp: () -> Unit,
) {
    BoxWithConstraints(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
        val scale = homeFrameScale
        Box(Modifier.size(360.dp * scale, 740.dp * scale)) {
            HomeStampTabs(
                stampSelected = stampSelected,
                onHome = onHome,
                onStamp = onStamp,
                modifier = Modifier.align(Alignment.BottomStart)
                    .padding(start = 16.dp * scale, bottom = 14.dp * scale),
                scale = scale,
            )
        }
    }
}

@Composable
private fun HomeStampTabs(
    stampSelected: Boolean,
    onHome: () -> Unit,
    onStamp: () -> Unit,
    modifier: Modifier,
    scale: Float,
) {
    val active = colorResource(R.color.entrance_create_background)
    Row(
        modifier.clip(RoundedCornerShape(40.dp * scale))
            .background(colorResource(R.color.main_tab_track))
            .padding(4.dp * scale)
            .selectableGroup(),
    ) {
        ToggleTab(
            label = stringResource(R.string.temp_home_tab),
            icon = if (stampSelected) R.drawable.main_tab_home_idle else R.drawable.main_tab_home_selected,
            color = colorResource(if (stampSelected) R.color.main_tab_home_idle else R.color.entrance_background),
            background = if (stampSelected) Color.Transparent else active,
            selected = !stampSelected,
            onClick = onHome,
            scale = scale,
        )
        ToggleTab(
            label = stringResource(R.string.wish_coupon_title),
            icon = if (stampSelected) R.drawable.main_tab_stamp_selected else R.drawable.main_tab_stamp_idle,
            color = colorResource(if (stampSelected) R.color.entrance_create_text else R.color.main_tab_stamp_idle),
            background = if (stampSelected) active else Color.Transparent,
            selected = stampSelected,
            onClick = onStamp,
            scale = scale,
        )
    }
}

@Composable
private fun ToggleTab(
    label: String,
    icon: Int,
    color: Color,
    background: Color,
    selected: Boolean,
    onClick: () -> Unit,
    scale: Float,
) {
    Column(
        modifier = Modifier.width(75.dp * scale).height(60.dp * scale)
            .clip(RoundedCornerShape(32.dp * scale))
            .background(background)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(28.dp * scale))
        Text(
            text = label,
            color = color,
            fontFamily = TabFont,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * scale).sp,
            lineHeight = (16.8 * scale).sp,
            letterSpacing = ((-0.36) * scale).sp,
            maxLines = 1,
        )
    }
}
