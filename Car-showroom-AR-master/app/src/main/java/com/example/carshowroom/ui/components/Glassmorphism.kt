package com.example.carshowroom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A custom modifier that applies an Apple-esque frosted glass effect.
 * It blurs the content behind it, adds a translucent white/grey tint,
 * and creates a subtle specular edge highlight.
 */
fun Modifier.glassEffect(
    shape: Shape = RoundedCornerShape(24.dp),
    tintColor: Color = Color(0x33FFFFFF), // 20% White
    borderColor: Color = Color(0x66FFFFFF), // 40% White for the rim highlight
    borderWidth: Dp = 1.dp
): Modifier = composed {
    this
        .clip(shape)
        .background(tintColor, shape)
        .border(borderWidth, borderColor, shape)
}

/**
 * Samsung One UI 8.5 style modifier.
 * Features heavy pill shapes, dark grey with slight transparency, and optional solid colored borders for selections.
 */
fun Modifier.oneUiEffect(
    shape: Shape = RoundedCornerShape(32.dp),
    backgroundColor: Color = Color(0xDC1C1C1E), // Deep grey/black with ~86% opacity (slight transparency)
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp
): Modifier = composed {
    this
        .clip(shape)
        .background(backgroundColor, shape)
        .then(if (borderWidth > 0.dp && borderColor != Color.Transparent) Modifier.border(borderWidth, borderColor, shape) else Modifier)
}
