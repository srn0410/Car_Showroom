package com.example.carshowroom.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carshowroom.ui.components.CarSelectorSheet
import com.example.carshowroom.ui.components.ColorPickerSheet
import com.example.carshowroom.ui.components.WheelPickerSheet
import com.example.carshowroom.ui.components.AnimatedBottomNavigationBar
import com.example.carshowroom.ui.components.BottomNavOption
import com.example.carshowroom.ui.components.oneUiEffect
import com.example.carshowroom.viewmodel.CarShowroomIntent
import com.example.carshowroom.viewmodel.CarShowroomViewModel
import io.github.sceneview.Scene
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironment
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes

@Composable
fun VirtualShowroomScreen(
    viewModel: CarShowroomViewModel,
    onNavigateToAR: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)
    
    // We can load a high quality HDRI environment here for beautiful reflections
    // For now we'll use a neutral default or from assets if available.
    val environment = rememberEnvironment(environmentLoader)

    val childNodes = rememberNodes()
    var currentModelNode by remember { mutableStateOf<ModelNode?>(null) }
    var currentZoomLevel by remember { mutableFloatStateOf(1.0f) }
    
    var isLoading by remember { mutableStateOf(false) }
    
    // Default SceneView camera is an orbit camera!
    val cameraNode = rememberCameraNode(engine)

    var showCarSheet by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }
    var showWheelSheet by remember { mutableStateOf(false) }

    // Handle Model Loading
    LaunchedEffect(uiState.selectedCar) {
        uiState.selectedCar?.let { car ->
            isLoading = true
            modelLoader.loadModelInstanceAsync(car.modelPath) { modelInstance ->
                modelInstance?.let { instance ->
                    val newModelNode = ModelNode(
                        modelInstance = instance,
                        scaleToUnits = 0.25f, // 1/4th the size so it fits on screen
                        centerOrigin = io.github.sceneview.math.Position(0f, 0f, 0f),
                    )

                    currentModelNode = newModelNode
                    currentZoomLevel = 1.0f // Reset zoom when car changes!
                    
                    childNodes.forEach { it.destroy() }
                    childNodes.clear()
                    childNodes.add(newModelNode)
                    
                    // Position the camera to look at the car
                    cameraNode.position = io.github.sceneview.math.Position(x = 0.0f, y = 0.5f, z = 2.5f)
                    cameraNode.lookAt(newModelNode)
                    
                    isLoading = false
                }
            }
        }
    }

    // Handle Color Change for Paint and Rims (Same exact logic as AR)
    LaunchedEffect(uiState.selectedColor, uiState.selectedWheel, currentModelNode) {
        currentModelNode?.let { modelNode ->
            val car = uiState.selectedCar
            val paintMaterials = car?.paintMaterialNames ?: emptyList()
            val rimMaterials = car?.rimMaterialNames ?: emptyList()
            
            modelNode.modelInstance.materialInstances.forEach { material ->
                val materialName = material.name ?: ""
                val isPaint = paintMaterials.isNotEmpty() && paintMaterials.any { 
                    materialName.contains(it, ignoreCase = true) || it.contains(materialName, ignoreCase = true)
                }
                val isRim = rimMaterials.isNotEmpty() && rimMaterials.any {
                    materialName.contains(it, ignoreCase = true) || it.contains(materialName, ignoreCase = true)
                }

                if (isPaint) {
                    uiState.selectedColor?.let { carColor ->
                        val c = carColor.color
                        if (car?.modelPath?.contains("4.glb") == true) {
                            material.setParameter("baseColorFactor", c.red, c.green, c.blue, c.alpha)
                            material.setParameter("emissiveFactor", c.red * 0.4f, c.green * 0.4f, c.blue * 0.4f)
                        } else {
                            val r = Math.pow(c.red.toDouble(), 2.2).toFloat()
                            val g = Math.pow(c.green.toDouble(), 2.2).toFloat()
                            val b = Math.pow(c.blue.toDouble(), 2.2).toFloat()
                            material.setParameter("baseColorFactor", r, g, b, c.alpha)
                            material.setParameter("emissiveFactor", 0f, 0f, 0f)
                        }
                    }
                } else if (isRim) {
                    uiState.selectedWheel?.let { wheelStyle ->
                        val c = wheelStyle.color
                        val r = Math.pow(c.red.toDouble(), 2.2).toFloat()
                        val g = Math.pow(c.green.toDouble(), 2.2).toFloat()
                        val b = Math.pow(c.blue.toDouble(), 2.2).toFloat()
                        material.setParameter("baseColorFactor", r, g, b, c.alpha)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // The 3D Scene
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            environmentLoader = environmentLoader,
            environment = environment,
            childNodes = childNodes,
            cameraNode = cameraNode,
            onFrame = {
                // We can add subtle auto-rotation here if we want!
            }
        )

        // Top Bar
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
                .oneUiEffect(shape = CircleShape)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Virtual Showroom", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                Text(text = "Swipe to spin car", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
        }

        // Mute Button
        IconButton(
            onClick = { viewModel.handleIntent(CarShowroomIntent.ToggleMute) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .oneUiEffect(shape = CircleShape)
        ) {
            Icon(
                imageVector = if (uiState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = "Toggle Mute",
                tint = Color.White
            )
        }

        // View in AR Button
        Button(
            onClick = onNavigateToAR,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .height(56.dp)
                .oneUiEffect(shape = RoundedCornerShape(28.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, // the oneUiEffect handles the background
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.ViewInAr, contentDescription = "View in AR", modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("View in AR", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        // Bottom Controls
        val currentSelection = when {
            showCarSheet -> BottomNavOption.CARS
            showColorSheet -> BottomNavOption.PAINT
            showWheelSheet -> BottomNavOption.WHEELS
            else -> null
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            AnimatedBottomNavigationBar(
                currentSelection = currentSelection,
                onOptionSelected = { option ->
                    if (currentSelection == option) {
                        showCarSheet = false
                        showColorSheet = false
                        showWheelSheet = false
                    } else {
                        showCarSheet = option == BottomNavOption.CARS
                        showColorSheet = option == BottomNavOption.PAINT
                        showWheelSheet = option == BottomNavOption.WHEELS
                    }
                }
            )
        }

        // Crossfade Loading Overlay
        androidx.compose.animation.AnimatedVisibility(
            visible = isLoading,
            modifier = Modifier.matchParentSize(),
            enter = androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(300)),
            exit = androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        // Sheets
        AnimatedVisibility(
            visible = showCarSheet,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CarSelectorSheet(
                    cars = uiState.cars,
                    selectedCar = uiState.selectedCar,
                    onCarSelected = { car ->
                        viewModel.handleIntent(CarShowroomIntent.SelectCar(car))
                        showCarSheet = false
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showColorSheet,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ColorPickerSheet(
                    colors = uiState.colors,
                    selectedColor = uiState.selectedColor,
                    onColorSelected = { color ->
                        viewModel.handleIntent(CarShowroomIntent.SelectColor(color))
                        showColorSheet = false
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = showWheelSheet,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                WheelPickerSheet(
                    wheels = uiState.wheels,
                    selectedWheel = uiState.selectedWheel,
                    onWheelSelected = { wheel ->
                        viewModel.handleIntent(CarShowroomIntent.SelectWheel(wheel))
                        showWheelSheet = false
                    }
                )
            }
        }
    }
}
