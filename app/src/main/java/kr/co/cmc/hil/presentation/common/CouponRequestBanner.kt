package kr.co.cmc.hil.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kr.co.cmc.hil.R
import kr.co.cmc.hil.presentation.stamp.wishStyle

/**
 * 파트너가 보낸 소원권 사용 요청이 있을 때 홈·스탬프 화면 위쪽에 띄우는 배너. 누르면 승인 화면으로 이동한다.
 */
@Composable
internal fun CouponRequestBanner(onClick: () -> Unit, modifier: Modifier = Modifier, scale: Float = 1f) {
    val actionLabel = stringResource(R.string.coupon_request_banner_action)

    Row(
        modifier = modifier
            .heightIn(min = 48.dp * scale)
            .clip(RoundedCornerShape(8.dp * scale))
            .background(colorResource(R.color.entrance_create_background))
            .clickable(role = Role.Button, onClickLabel = actionLabel, onClick = onClick)
            .padding(horizontal = 14.dp * scale, vertical = 10.dp * scale),
        horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.coupon_request_banner),
            color = Color.White,
            style = wishStyle(14f * scale, FontWeight.Medium),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = actionLabel,
            color = colorResource(R.color.entrance_background),
            style = wishStyle(14f * scale, FontWeight.SemiBold),
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun CouponRequestBannerPreview() {
    CouponRequestBanner(onClick = {})
}
