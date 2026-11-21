package kr.co.cmc.hil.presentation.invitation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kr.co.cmc.hil.R
import kotlin.math.roundToInt

@Composable
internal fun InvitationArtwork(modifier: Modifier = Modifier) {
    val back = invitationBitmap(R.drawable.invitation_envelope_back)
    val glow = invitationBitmap(R.drawable.invitation_envelope_glow)
    val left = invitationBitmap(R.drawable.invitation_envelope_left)
    val right = invitationBitmap(R.drawable.invitation_envelope_right)
    val front = invitationBitmap(R.drawable.invitation_envelope_front)
    val orange = colorResource(R.color.entrance_create_background)
    val gold = colorResource(R.color.invitation_card_light)
    val background = colorResource(R.color.entrance_background)

    Canvas(modifier) {
        val unit = size.width / 360f
        clipRect {
            drawLayer(back, unit, -12f, 295f, 385f, 359f)
            drawRoundRect(
                color = orange,
                topLeft = Offset(12f * unit, 185f * unit),
                size = Size(337f * unit, 355f * unit),
                cornerRadius = CornerRadius(20f * unit),
            )
            // Scale the radial falloff independently on each axis, as in the Figma card.
            val center = Offset(181f * unit, 504f * unit)
            scale(scaleX = 357.71f / 165.5f, scaleY = 1f, pivot = center) {
                val stretch = 357.71f / 165.5f
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(gold, gold.copy(alpha = 0f)),
                        center = center,
                        radius = 165.5f * unit,
                    ),
                    topLeft = Offset((181f + (12f - 181f) / stretch) * unit, 185f * unit),
                    size = Size(337f * unit / stretch, 355f * unit),
                    cornerRadius = CornerRadius(20f * unit / stretch, 20f * unit),
                )
            }
            drawLayer(glow, unit, -78f, 247f, 518f, 354f)
            drawSide(left, unit, 23f, mirrored = false)
            drawSide(right, unit, 338f, mirrored = true)
            drawLayer(front, unit, -12f, 488f, 385f, 166f)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(background.copy(alpha = 0f), background),
                    startY = 488f * unit,
                    endY = 740f * unit,
                ),
                topLeft = Offset(0f, 488f * unit),
                size = Size(size.width, 252f * unit),
            )
        }
    }
}

@Composable
private fun invitationBitmap(@DrawableRes drawable: Int): ImageBitmap =
    ImageBitmap.imageResource(drawable)

private fun DrawScope.drawSide(image: ImageBitmap, unit: Float, centerX: Float, mirrored: Boolean) {
    val centerY = 529.5f
    val pivot = Offset(centerX * unit, centerY * unit)
    rotate(90f, pivot) {
        scale(1f, if (mirrored) -1f else 1f, pivot) {
            drawLayer(image, unit, centerX - 124.5f, centerY - 35f, 249f, 70f)
        }
    }
}

private fun DrawScope.drawLayer(
    image: ImageBitmap,
    unit: Float,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
) {
    drawImage(
        image = image,
        dstOffset = IntOffset((left * unit).roundToInt(), (top * unit).roundToInt()),
        dstSize = IntSize((width * unit).roundToInt(), (height * unit).roundToInt()),
    )
}
