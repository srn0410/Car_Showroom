package com.example.carshowroom.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

enum class BottomNavOption(val title: String, val icon: ImageVector) {
    CARS("Cars", Icons.Default.DirectionsCar),
    PAINT("Paint", Icons.Default.ColorLens),
    WHEELS("Rims", Icons.Default.DonutLarge)
}

@Composable
fun AnimatedBottomNavigationBar(
    currentSelection: BottomNavOption?,
    onOptionSelected: (BottomNavOption) -> Unit
) {
    val itemWidths = remember { mutableStateMapOf<BottomNavOption, Float>() }
    val itemOffsets = remember { mutableStateMapOf<BottomNavOption, Float>() }

    var targetWidth by remember { mutableStateOf(0f) }
    var targetOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(currentSelection, itemWidths.size, itemOffsets.size) {
        if (currentSelection != null) {
            targetWidth = itemWidths[currentSelection] ?: 0f
            targetOffset = itemOffsets[currentSelection] ?: 0f
        }
    }

    val animatedOffset by animateFloatAsState(
        targetValue = targetOffset,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioLowBouncy)
    )
    
    val animatedWidth by animateFloatAsState(
        targetValue = targetWidth,
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    
    val animatedAlpha by animateFloatAsState(
        targetValue = if (currentSelection != null) 1f else 0f,
        animationSpec = tween(300)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            .oneUiEffect(shape = RoundedCornerShape(32.dp))
    ) {
        // The Sliding Indicator Layer
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                    .width(with(LocalDensity.current) { animatedWidth.toDp() })
                    .fillMaxHeight()
                    .padding(vertical = 6.dp) // Slight padding so it doesn't touch the top/bottom edges
                    .alpha(animatedAlpha)
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(26.dp))
            )
        }

        // The Items Layer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), // Removed horizontal padding from row so items can spread fully
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BottomNavOption.values().forEach { option ->
                val isSelected = currentSelection == option
                Column(
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            itemWidths[option] = coords.size.width.toFloat()
                            itemOffsets[option] = coords.positionInParent().x
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // Removes the default ripple effect since we have the sliding pill
                        ) {
                            // If they tap the already selected item, we deselect it (close sheet)
                            if (isSelected) {
                                // Since we don't have a direct "deselect" callback, 
                                // we actually handle deselection in the parent by listening to the same option
                                onOptionSelected(option)
                            } else {
                                onOptionSelected(option)
                            }
                        }
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = option.icon,
                        contentDescription = option.title,
                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = option.title,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                    )
                }
            }
        }
    }
}
