package com.example.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaturationSlider(
    hue: Float,
    saturation: Float,
    value: Float,
    onSaturationChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val startColor = Color.hsv(hue, 0f, value)
    val endColor = Color.hsv(hue, 1f, value)
    
    Slider(
        value = saturation,
        onValueChange = onSaturationChange,
        valueRange = 0f..1f,
        modifier = modifier,
        thumb = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.White, CircleShape)
            )
        },
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Brush.horizontalGradient(listOf(startColor, endColor)), RoundedCornerShape(4.dp))
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValueSlider(
    hue: Float,
    saturation: Float,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val startColor = Color.Black
    val endColor = Color.hsv(hue, saturation, 1f)
    
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = 0f..1f,
        modifier = modifier,
        thumb = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.White, CircleShape)
            )
        },
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Brush.horizontalGradient(listOf(startColor, endColor)), RoundedCornerShape(4.dp))
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphaSlider(
    color: Color,
    alpha: Float,
    onAlphaChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val startColor = color.copy(alpha = 0f)
    val endColor = color.copy(alpha = 1f)
    
    Slider(
        value = alpha,
        onValueChange = onAlphaChange,
        valueRange = 0f..1f,
        modifier = modifier,
        thumb = {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(Color.White, CircleShape)
            )
        },
        track = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Brush.horizontalGradient(listOf(startColor, endColor)), RoundedCornerShape(4.dp))
            )
        }
    )
}
