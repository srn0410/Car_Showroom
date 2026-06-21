package com.example.carshowroom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.carshowroom.data.models.CarColor

@Composable
fun ColorPickerSheet(
    colors: List<CarColor>,
    selectedColor: CarColor?,
    onColorSelected: (CarColor) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Select Color",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(colors) { carColor ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(carColor.color)
                        .border(
                            width = 3.dp,
                            color = if (carColor == selectedColor) MaterialTheme.colorScheme.primary 
                                    else androidx.compose.ui.graphics.Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(carColor) }
                )
            }
        }
    }
}
