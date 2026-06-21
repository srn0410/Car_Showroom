package com.example.carshowroom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun RevButton(
    onRevvingChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.Red)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onRevvingChanged(true)
                        tryAwaitRelease()
                        onRevvingChanged(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Using Settings icon as a placeholder for steering wheel
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Rev",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}
