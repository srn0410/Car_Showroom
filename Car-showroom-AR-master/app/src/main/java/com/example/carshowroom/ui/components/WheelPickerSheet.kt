package com.example.carshowroom.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    ) {
        Text(
            text = "Select Wheels",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(wheels) { wheel ->
                Card(
                    modifier = Modifier
                        .width(120.dp)
                        .height(100.dp)
                        .clickable { onWheelSelected(wheel) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (wheel == selectedWheel) MaterialTheme.colorScheme.primaryContainer 
                                         else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = wheel.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
