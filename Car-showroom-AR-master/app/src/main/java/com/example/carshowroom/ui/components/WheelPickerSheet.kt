package com.example.carshowroom.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carshowroom.data.models.WheelStyle

@Composable
fun WheelPickerSheet(
    wheels: List<WheelStyle>,
    selectedWheel: WheelStyle?,
    onWheelSelected: (WheelStyle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .oneUiEffect()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Rim Color",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(wheels) { wheel ->
                val isSelected = wheel == selectedWheel
                val size by animateDpAsState(
                    targetValue = if (isSelected) 56.dp else 48.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                    label = "wheelSize"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onWheelSelected(wheel) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape)
                            .background(wheel.color)
                            .border(
                                width = if (isSelected) 4.dp else 2.dp,
                                color = if (isSelected) Color(0xFFFF3B30) else Color.White.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = wheel.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
