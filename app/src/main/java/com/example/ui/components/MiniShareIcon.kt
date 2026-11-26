package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
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
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform

/**
 * Mobile app icon with liquid/frosted glass effect rendered purely in Kotlin Compose Canvas.
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
                drawGlassBackground()
            }

            // Scale cleanly inside the 108x108 tile so the frosted glass card has crisp breathing room
            val foregroundScale = if (includeBackground) 1.14f else 1f
            withTransform({
                scale(
                    scaleX = foregroundScale,
                    scaleY = foregroundScale,
                    pivot = Offset(54f, 54f)
                )
            }) {
                // Frosted glass backdrop tile (inside 66dp safe zone 22.5..85.5)
                drawFrostedGlassBackdrop()

                // Left Blue Phone (-10 deg)
                drawStylizedPhone(
                    centerX = 40.5f,
                    centerY = 44.5f,
                    rotationDegrees = -10f,
                    shadowColor = Color(0x26081C4D),
                    rimColors = listOf(
                        Color(0xFFEF3B36),
                        Color(0xFF28C76F),
                        Color(0xFF246BFD),
                        Color(0xFFF6B93B),
                        Color(0xFFEF3B36)
                    ),
                    bodyTop = Color(0xFF1F50CC),
                    bodyBottom = Color(0xFF0E2B7A),
                    screenTop = Color(0xFF3980FF),
                    screenBottom = Color(0xFF1341AE),
                    bottomBounceTint = Color(0x38B8E1FF),
                    speakerColor = Color(0xFF091B4C),
                    homeFill = Color(0xFF0C2464),
                    homeRing = Color(0xCC8BB6FF)
                )

                // Right Emerald Phone (+10 deg)
                drawStylizedPhone(
                    centerX = 67.5f,
                    centerY = 44.5f,
                    rotationDegrees = 10f,
                    shadowColor = Color(0x26052B20),
                    rimColors = listOf(
                        Color(0xFFF79F1F),
                        Color(0xFF16B877),
                        Color(0xFF1E88E5),
                        Color(0xFFE84393),
                        Color(0xFFF79F1F)
                    ),
                    bodyTop = Color(0xFF178D6D),
                    bodyBottom = Color(0xFF0A4938),
                    screenTop = Color(0xFF2DD19F),
                    screenBottom = Color(0xFF0D7156),
                    bottomBounceTint = Color(0x38A8FFE5),
                    speakerColor = Color(0xFF063024),
                    homeFill = Color(0xFF084233),
                    homeRing = Color(0xCC7DF2CF)
                )

                // Curved double-headed crimson smile arrow with glass reflection
                drawSmileArrow()
            }
        }
    }
}

private fun DrawScope.drawGlassBackground() {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFC5DCFF),
                Color(0xFFD2F3F5),
                Color(0xFFB6F4E0)
            ),
            start = Offset(4f, 4f),
            end = Offset(104f, 104f)
        ),
        topLeft = Offset.Zero,
        size = Size(108f, 108f)
    )

    // Top-left luminous frosted glow
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0x8CFFFFFF),
                0.48f to Color(0x3DE6F7FF),
                1.0f to Color.Transparent
            ),
            center = Offset(24f, 20f),
            radius = 68f
        ),
        topLeft = Offset.Zero,
        size = Size(108f, 108f)
    )

    // Bottom-right mint-cyan caustic glow
    drawRect(
        brush = Brush.radialGradient(
            colorStops = arrayOf(
                0.0f to Color(0x6178FBE2),
                1.0f to Color.Transparent
            ),
            center = Offset(86f, 88f),
            radius = 56f
        ),
        topLeft = Offset.Zero,
        size = Size(108f, 108f)
    )

    // Subtle diagonal glass refraction sweep
    val sweepPath = Path().apply {
        moveTo(0f, 0f)
        lineTo(76f, 0f)
        lineTo(28f, 108f)
        lineTo(0f, 108f)
        close()
    }
    drawPath(
        path = sweepPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0x38FFFFFF),
                Color.Transparent
            ),
            start = Offset.Zero,
            end = Offset(60f, 86f)
        )
    )
}

private fun DrawScope.drawFrostedGlassBackdrop() {
    val cardPath = Path().apply {
        addRoundRect(
            RoundRect(
                left = 22.5f,
                top = 22.5f,
                right = 85.5f,
                bottom = 85.5f,
                cornerRadius = CornerRadius(15f, 15f)
            )
        )
    }

    // Soft ambient shadow behind glass tile
    drawRoundRect(
        color = Color(0x1C0E2B7A),
        topLeft = Offset(22.5f, 24.0f),
        size = Size(63f, 63f),
        cornerRadius = CornerRadius(15f, 15f)
    )

    // Frosted translucent milky fill
    drawPath(
        path = cardPath,
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0x85FFFFFF),
                0.48f to Color(0x4DFFFFFF),
                1.0f to Color(0x28E8FAFF)
            ),
            start = Offset(23f, 23f),
            end = Offset(85f, 85f)
        )
    )

    // Diagonal internal glass sheen clipped to the rounded tile
    clipPath(cardPath) {
        val sheenPath = Path().apply {
            moveTo(22.5f, 22.5f)
            lineTo(85.5f, 22.5f)
            lineTo(85.5f, 33.0f)
            lineTo(22.5f, 64.0f)
            close()
        }
        drawPath(
            path = sheenPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0x4DFFFFFF),
                    Color(0x05FFFFFF)
                ),
                start = Offset(24f, 23f),
                end = Offset(62f, 56f)
            )
        )
    }

    // Crisp luminous glass rim + directional bevel highlights
    drawPath(
        path = cardPath,
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.0f to Color(0xEBFFFFFF),
                0.45f to Color(0x66FFFFFF),
                1.0f to Color(0x707AFBE4)
            ),
            start = Offset(22.5f, 22.5f),
            end = Offset(85.5f, 85.5f)
        ),
        style = Stroke(width = 0.95f)
    )
}

private fun DrawScope.drawStylizedPhone(
    centerX: Float,
    centerY: Float,
    rotationDegrees: Float,
    shadowColor: Color,
    rimColors: List<Color>,
    bodyTop: Color,
    bodyBottom: Color,
    screenTop: Color,
    screenBottom: Color,
    bottomBounceTint: Color,
    speakerColor: Color,
    homeFill: Color,
    homeRing: Color
) {
    rotate(degrees = rotationDegrees, pivot = Offset(centerX, centerY)) {
        val left = centerX - 9f
        val top = centerY - 14f
        val width = 18f
        val height = 28f

        // Soft floating shadow
        drawRoundRect(
            color = shadowColor,
            topLeft = Offset(left + 0.5f, top + 1.3f),
            size = Size(width, height),
            cornerRadius = CornerRadius(3.8f, 3.8f)
        )

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

        // Crisp top-left glass specular edge on outer phone frame
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xB3FFFFFF),
                    Color.Transparent
                ),
                start = Offset(left, top),
                end = Offset(left + width * 0.72f, top + height * 0.65f)
            ),
            topLeft = Offset(left + 0.25f, top + 0.25f),
            size = Size(width - 0.5f, height - 0.5f),
            cornerRadius = CornerRadius(3.55f, 3.55f),
            style = Stroke(width = 0.45f)
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
                RoundRect(
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

        // Dual-layer liquid glass reflections clipped to screen
        clipPath(screenPath) {
            val topSheen = Path().apply {
                moveTo(screenLeft, screenTopY)
                lineTo(screenLeft + screenW, screenTopY)
                lineTo(screenLeft + screenW, screenTopY + 5.5f)
                lineTo(screenLeft, screenTopY + 14.9f)
                close()
            }
            drawPath(
                path = topSheen,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0x75FFFFFF),
                        Color(0x18FFFFFF)
                    ),
                    start = Offset(screenLeft, screenTopY),
                    end = Offset(screenLeft + screenW, screenTopY + 12f)
                )
            )

            val bottomGlow = Path().apply {
                moveTo(screenLeft, screenTopY + 15.1f)
                lineTo(screenLeft + screenW, screenTopY + 12.3f)
                lineTo(screenLeft + screenW, screenTopY + screenH)
                lineTo(screenLeft, screenTopY + screenH)
                close()
            }
            drawPath(
                path = bottomGlow,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        bottomBounceTint
                    ),
                    startY = screenTopY + 13.6f,
                    endY = screenTopY + screenH
                )
            )
        }

        // Inner screen glass rim glint
        drawPath(
            path = screenPath,
            color = Color(0x70FFFFFF),
            style = Stroke(width = 0.32f)
        )

        // Home button
        val homeCenter = Offset(centerX, top + height - 3.65f)
        drawCircle(
            color = homeFill,
            radius = 1.35f,
            center = homeCenter
        )
        drawCircle(
            color = homeRing,
            radius = 1.35f,
            center = homeCenter,
            style = Stroke(width = 0.38f)
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

    // Soft floating shadow under smile arrow
    translate(left = 0f, top = 1.2f) {
        drawPath(
            path = arrowPath,
            color = Color(0x26280305)
        )
    }

    // Crimson metallic-glass gradient fill
    drawPath(
        path = arrowPath,
        brush = Brush.horizontalGradient(
            colorStops = arrayOf(
                0.00f to Color(0xFF300406),
                0.22f to Color(0xFF9C0E14),
                0.50f to Color(0xFFF2242B),
                0.78f to Color(0xFF9C0E14),
                1.00f to Color(0xFF300406)
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

    // Molded glossy glass top-edge reflection along the curved smile
    val topSheen = Path().apply {
        moveTo(34.2f, 65.2f)
        cubicTo(42.3f, 73.4f, 65.7f, 73.4f, 73.8f, 65.2f)
        lineTo(74.7f, 66.0f)
        cubicTo(66.1f, 75.0f, 41.9f, 75.0f, 33.3f, 66.0f)
        close()
    }
    drawPath(
        path = topSheen,
        brush = Brush.horizontalGradient(
            colorStops = arrayOf(
                0.00f to Color(0x12FFFFFF),
                0.50f to Color(0x6EFFFFFF),
                1.00f to Color(0x12FFFFFF)
            ),
            startX = 33.3f,
            endX = 74.7f
        )
    )
}
