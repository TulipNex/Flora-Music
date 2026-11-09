package com.example.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun RoundColorPicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onColorChange: (newHue: Float, newSat: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .size(170.dp)
            .onSizeChanged { canvasSize = it }
            .pointerInput(canvasSize) {
                detectDragGestures(
                    onDragStart = { offset ->
                        handleTouch(offset, canvasSize, onColorChange)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        handleTouch(change.position, canvasSize, onColorChange)
                    }
                )
            }
            .pointerInput(canvasSize) {
                detectTapGestures { offset ->
                    handleTouch(offset, canvasSize, onColorChange)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (min(size.width, size.height) / 2f) - 6f

            // 1. Chromatic sweep gradient for all 360° hues
            val hueColors = listOf(
                Color.Red,
                Color.Yellow,
                Color.Green,
                Color.Cyan,
                Color.Blue,
                Color.Magenta,
                Color.Red
            )
            drawCircle(
                brush = Brush.sweepGradient(hueColors, center),
                radius = radius,
                center = center
            )

            // 2. Radial gradient from White (center) to Transparent (edge) for Saturation
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // 3. Dark overlay if brightness / value < 1.0f
            if (value < 1.0f) {
                drawCircle(
                    color = Color.Black.copy(alpha = (1.0f - value).coerceIn(0f, 0.95f)),
                    radius = radius,
                    center = center
                )
            }

            // 4. Indicator thumb
            val angleRad = Math.toRadians(hue.toDouble())
            val thumbDist = saturation.coerceIn(0f, 1f) * radius
            val thumbX = center.x + (thumbDist * cos(angleRad)).toFloat()
            val thumbY = center.y + (thumbDist * sin(angleRad)).toFloat()
            val thumbCenter = Offset(thumbX, thumbY)

            val currentColor = try {
                Color.hsv(hue, saturation.coerceIn(0f, 1f), value.coerceIn(0f, 1f))
            } catch (e: Exception) {
                Color.White
            }

            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = 12.dp.toPx(),
                center = thumbCenter
            )
            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = thumbCenter
            )
            drawCircle(
                color = currentColor,
                radius = 7.dp.toPx(),
                center = thumbCenter
            )
        }
    }
}

private fun handleTouch(
    offset: Offset,
    size: IntSize,
    onColorChange: (Float, Float) -> Unit
) {
    if (size.width <= 0 || size.height <= 0) return
    val cx = size.width / 2f
    val cy = size.height / 2f
    val dx = offset.x - cx
    val dy = offset.y - cy
    val dist = hypot(dx.toDouble(), dy.toDouble()).toFloat()
    val maxRadius = (min(size.width, size.height) / 2f) - 6f
    if (maxRadius <= 0f) return

    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
    if (angle < 0) angle += 360f

    val sat = (dist / maxRadius).coerceIn(0f, 1f)
    onColorChange(angle, sat)
}
