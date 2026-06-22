package com.example.carshowroom.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carshowroom.data.models.CarModel

@Composable
fun CarSelectorSheet(
    cars: List<CarModel>,
    selectedCar: CarModel?,
    onCarSelected: (CarModel) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .oneUiEffect()
            .padding(16.dp)
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    ) {
        Text(
            text = "Select Car",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            items(cars) { car ->
                CarCard(
                    car = car,
                    isSelected = car == selectedCar,
                    onClick = { onCarSelected(car) }
                )
            }
        }
    }
}

@Composable
fun CarCard(
    car: CarModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(if (isSelected) 140.dp else 120.dp)
            .height(if (isSelected) 110.dp else 90.dp)
            .animateContentSize(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
            .oneUiEffect(
                shape = RoundedCornerShape(24.dp),
                backgroundColor = if (isSelected) Color(0xDC2C2C2E) else Color(0xDC1C1C1E),
                borderColor = if (isSelected) Color(0xFFFF3B30) else Color.Transparent,
                borderWidth = if (isSelected) 2.dp else 0.dp
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = car.name,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
            color = Color.White
        )
    }
}
