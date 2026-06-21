package com.example.carshowroom.data.models

import androidx.compose.ui.graphics.Color

data class CarColor(
    val name: String,
    val color: Color
)

fun Color.toFilamentColor(): FloatArray {
    return floatArrayOf(red, green, blue, alpha)
}
