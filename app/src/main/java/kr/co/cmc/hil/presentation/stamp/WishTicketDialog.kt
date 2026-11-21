package kr.co.cmc.hil.presentation.stamp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kr.co.cmc.hil.R

@Composable
internal fun WishTicketDialog(onDismiss: () -> Unit, onUse: () -> Unit) {
    val orange = colorResource(R.color.entrance_create_background)
    val ink = colorResource(R.color.entrance_background)
    val ticketShape = RoundedCornerShape(14.dp)
    val issuedDate = rememberSaveable { LocalDate.now().toEpochDay() }
    val issued = LocalDate.ofEpochDay(issuedDate)
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
                .widthIn(max = 260.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                Modifier.fillMaxWidth().heightIn(min = 282.dp)
                    .clip(ticketShape).background(orange),
            ) {
                Image(
                    painterResource(R.drawable.wish_start),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomStart).offset(x = (-26).dp, y = 22.dp).size(160.dp),
                    colorFilter = ColorFilter.tint(colorResource(R.color.wish_ticket_decoration)),
                )
                Image(
                    painterResource(R.drawable.wish_start),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 38.dp, y = 10.dp).size(190.dp),
                    colorFilter = ColorFilter.tint(colorResource(R.color.wish_ticket_decoration)),
                )
                Column(
                    modifier = Modifier.align(Alignment.TopCenter).padding(start = 14.dp, top = 53.dp, end = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        stringResource(R.string.wish_coupon_title),
                        color = Color.White,
                        style = wishStyle(34f, FontWeight.SemiBold),
                    )
                    Text(
                        stringResource(R.string.wish_ticket_body),
                        color = Color.White,
                        style = wishStyle(18f, FontWeight.Medium),
                        textAlign = TextAlign.Center,
                    )
                }
                val closeLabel = stringResource(R.string.wish_coupon_close)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).semantics { contentDescription = closeLabel },
                ) {
                    Text(stringResource(R.string.wish_ticket_close_icon), color = ink, style = wishStyle(25f))
                }
            }
            Row(
                Modifier.fillMaxWidth().heightIn(min = 72.dp)
                    .background(orange, ticketShape).padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.wish_ticket_period, issued.format(formatter), issued.plusMonths(1).format(formatter)),
                    modifier = Modifier.weight(1f),
                    color = ink,
                    style = wishStyle(14f),
                )
                Button(
                    onClick = onUse,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ink, contentColor = Color.White),
                ) {
                    Text(stringResource(R.string.wish_ticket_use), style = wishStyle(17f, FontWeight.Medium))
                }
            }
        }
    }
}
