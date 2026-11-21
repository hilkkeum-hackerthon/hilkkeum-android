package kr.co.cmc.hil.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
private val ToggleShape = RoundedCornerShape(40.dp)
private val TabShape = RoundedCornerShape(32.dp)

@Composable
fun HomeStampToggle(
    stampSelected: Boolean,
    onHome: () -> Unit,
    onStamp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = colorResource(R.color.entrance_create_background)
    Row(
        modifier.clip(ToggleShape)
            .background(colorResource(R.color.main_tab_track))
            .padding(4.dp)
            .selectableGroup(),
    ) {
        ToggleTab(
            label = stringResource(R.string.temp_home_tab),
            icon = if (stampSelected) R.drawable.main_tab_home_idle else R.drawable.main_tab_home_selected,
            color = colorResource(if (stampSelected) R.color.main_tab_home_idle else R.color.entrance_background),
            background = if (stampSelected) Color.Transparent else active,
            selected = !stampSelected,
            onClick = onHome,
        )
        ToggleTab(
            label = stringResource(R.string.temp_home_stamp),
            icon = if (stampSelected) R.drawable.main_tab_stamp_selected else R.drawable.main_tab_stamp_idle,
            color = colorResource(if (stampSelected) R.color.entrance_create_text else R.color.main_tab_stamp_idle),
            background = if (stampSelected) active else Color.Transparent,
            selected = stampSelected,
            onClick = onStamp,
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
) {
    Column(
        modifier = Modifier.width(71.dp).height(48.8.dp)
            .clip(TabShape)
            .background(background)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
        Text(
            text = label,
            color = color,
            fontFamily = TabFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.8.sp,
            letterSpacing = (-0.36).sp,
            maxLines = 1,
        )
    }
}
