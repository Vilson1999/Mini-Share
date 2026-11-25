package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Mobile app icon rendered purely in Kotlin Compose Canvas.
 */
@Composable
fun MiniShareIcon(
    modifier: Modifier = Modifier,
    includeBackground: Boolean = true
) {
    Canvas(modifier = modifier) {
        val unit = minOf(size.width, size.height) / 108f
        val offsetX = (size.width - 108f * unit) / 2f
        val offsetY = (size.height - 108f * unit) / 2f

        withTransform({
            translate(left = offsetX, top = offsetY)
            scale(scaleX = unit, scaleY = unit, pivot = Offset.Zero)
        }) {
            if (includeBackground) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFC5DCFF),
                            Color(0xFFD2F3F5),
                            Color(0xFFB6F4E0)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(108f, 108f)
                    ),
                    topLeft = Offset.Zero,
                    size = Size(108f, 108f)
                )
            }

            // Slightly enlarge foreground group inside non-masked in-app square
            withTransform({
                scale(
                    scaleX = if (includeBackground) 1.24f else 1f,
                    scaleY = if (includeBackground) 1.24f else 1f,
                    pivot = Offset(54f, 54f)
                )
            }) {
                // Left Blue Phone (-10 deg)
                drawStylizedPhone(
                    centerX = 40.5f,
                    centerY = 44.5f,
                    rotationDegrees = -10f,
                    rimColors = listOf(
                        Color(0xFFEF3B36),
                        Color(0xFF28C76F),
                        Color(0xFF246BFD),
                        Color(0xFFF6B93B),
                        Color(0xFFEF3B36)
                    ),
                    bodyTop = Color(0xFF1A46B8),
                    bodyBottom = Color(0xFF0E2B7A),
                    screenTop = Color(0xFF2B6EF2),
                    screenBottom = Color(0xFF1341AE),
                    speakerColor = Color(0xFF091B4C),
                    homeFill = Color(0xFF0C2464),
                    homeRing = Color(0xFF6B9CF6)
                )

                // Right Emerald Phone (+10 deg)
                drawStylizedPhone(
                    centerX = 67.5f,
                    centerY = 44.5f,
                    rotationDegrees = 10f,
                    rimColors = listOf(
                        Color(0xFFF79F1F),
                        Color(0xFF16B877),
                        Color(0xFF1E88E5),
                        Color(0xFFE84393),
                        Color(0xFFF79F1F)
                    ),
                    bodyTop = Color(0xFF127A5E),
                    bodyBottom = Color(0xFF0A4938),
                    screenTop = Color(0xFF25B88B),
                    screenBottom = Color(0xFF0D7156),
                    speakerColor = Color(0xFF063024),
                    homeFill = Color(0xFF084233),
                    homeRing = Color(0xFF5CE0BA)
                )

                // Curved double-headed crimson smile arrow
                drawSmileArrow()
            }
        }
    }
}

private fun DrawScope.drawStylizedPhone(
    centerX: Float,
    centerY: Float,
    rotationDegrees: Float,
    rimColors: List<Color>,
    bodyTop: Color,
    bodyBottom: Color,
    screenTop: Color,
    screenBottom: Color,
    speakerColor: Color,
    homeFill: Color,
    homeRing: Color
) {
    rotate(degrees = rotationDegrees, pivot = Offset(centerX, centerY)) {
        val left = centerX - 9f
        val top = centerY - 14f
        val width = 18f
        val height = 28f

        // Rainbow outer border
        drawRoundRect(
            brush = Brush.sweepGradient(
                colors = rimColors,
                center = Offset(centerX, centerY)
            ),
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(3.8f, 3.8f)
        )

        // Phone bezel body
        val bezelInset = 1.35f
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(bodyTop, bodyBottom),
                start = Offset(left, top),
                end = Offset(left + width, top + height)
            ),
            topLeft = Offset(left + bezelInset, top + bezelInset),
            size = Size(width - bezelInset * 2f, height - bezelInset * 2f),
            cornerRadius = CornerRadius(2.7f, 2.7f)
        )

        // Speaker slot
        drawRoundRect(
            color = speakerColor,
            topLeft = Offset(centerX - 2.3f, top + 2.5f),
            size = Size(4.6f, 0.85f),
            cornerRadius = CornerRadius(0.45f, 0.45f)
        )

        // Screen area
        val screenLeft = centerX - 6.1f
        val screenTopY = top + 4.4f
        val screenW = 12.2f
        val screenH = 17.4f
        val screenPath = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = screenLeft,
                    top = screenTopY,
                    right = screenLeft + screenW,
                    bottom = screenTopY + screenH,
                    cornerRadius = CornerRadius(1.2f, 1.2f)
                )
            )
        }
        drawPath(
            path = screenPath,
            brush = Brush.linearGradient(
                colors = listOf(screenTop, screenBottom),
                start = Offset(screenLeft, screenTopY),
                end = Offset(screenLeft + screenW, screenTopY + screenH)
            )
        )

        // Glossy diagonal glass sheen
        clipPath(screenPath) {
            val sheen = Path().apply {
                moveTo(screenLeft, screenTopY)
                lineTo(screenLeft + screenW, screenTopY)
                lineTo(screenLeft + screenW, screenTopY + 5.2f)
                lineTo(screenLeft, screenTopY + 14.8f)
                close()
            }
            drawPath(
                path = sheen,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        Color.White.copy(alpha = 0.05f)
                    ),
                    start = Offset(screenLeft, screenTopY),
                    end = Offset(screenLeft + screenW, screenTopY + 12f)
                )
            )
        }

        // Home button
        val homeCenter = Offset(centerX, top + height - 3.65f)
        drawCircle(
            color = homeFill,
            radius = 1.35f,
            center = homeCenter
        )
        drawCircle(
            color = homeRing.copy(alpha = 0.65f),
            radius = 1.35f,
            center = homeCenter,
            style = Stroke(width = 0.35f)
        )
    }
}

private fun DrawScope.drawSmileArrow() {
    val arrowPath = Path().apply {
        moveTo(27.5f, 61.0f)
        lineTo(35.8f, 63.2f)
        lineTo(33.6f, 65.0f)
        cubicTo(42.0f, 73.8f, 66.0f, 73.8f, 74.4f, 65.0f)
        lineTo(72.2f, 63.2f)
        lineTo(80.5f, 61.0f)
        lineTo(78.5f, 69.6f)
        lineTo(76.6f, 67.9f)
        cubicTo(66.5f, 79.8f, 41.5f, 79.8f, 31.4f, 67.9f)
        lineTo(29.5f, 69.6f)
        close()
    }

    drawPath(
        path = arrowPath,
        brush = Brush.horizontalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF280305),
                0.22f to Color(0xFF8C0B10),
                0.50f to Color(0xFFE61B22),
                0.78f to Color(0xFF8C0B10),
                1.00f to Color(0xFF280305)
            ),
            startX = 27.5f,
            endX = 80.5f
        )
    )

    drawPath(
        path = arrowPath,
        color = Color(0xFF1F0203).copy(alpha = 0.55f),
        style = Stroke(width = 0.55f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
}
