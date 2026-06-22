package com.example.carshowroom.ui

import android.view.MotionEvent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.carshowroom.ui.components.CarSelectorSheet
import com.example.carshowroom.ui.components.ColorPickerSheet
import com.example.carshowroom.ui.components.WheelPickerSheet
import com.example.carshowroom.ui.components.AnimatedBottomNavigationBar
import com.example.carshowroom.ui.components.BottomNavOption
import com.example.carshowroom.ui.components.oneUiEffect
import com.example.carshowroom.utils.ImageUtils
import com.example.carshowroom.viewmodel.CarShowroomIntent
import com.example.carshowroom.viewmodel.CarShowroomViewModel
import com.example.carshowroom.viewmodel.InteractionMode
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArSceneScreen(
    viewModel: CarShowroomViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showCarSheet by remember { mutableStateOf(value = false) }
    var showColorSheet by remember { mutableStateOf(value = false) }
    var showWheelSheet by remember { mutableStateOf(value = false) }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = io.github.sceneview.rememberEnvironmentLoader(engine)
    val environment = io.github.sceneview.rememberEnvironment(environmentLoader)
    val childNodes = rememberNodes()
    var currentAnchorNode by remember { mutableStateOf<AnchorNode?>(null) }
    var currentModelNode by remember { mutableStateOf<ModelNode?>(null) }
    var lastTapEvent by remember { mutableStateOf<MotionEvent?>(null) }
    var currentZoomLevel by remember { mutableFloatStateOf(1.0f) }

    var isCapturing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(isCapturing) {
        if (isCapturing) {
            // Give Compose time to re-render without the UI (2 frames)
            delay(100)
            ImageUtils.captureArSnapshot(context, (context as android.app.Activity).window)
            // Restore UI
            isCapturing = false
        }
    }

    // Handle Model Loading/Switching
    LaunchedEffect(uiState.selectedCar) {
        uiState.selectedCar?.let { car ->
            modelLoader.loadModelInstanceAsync(car.modelPath) { modelInstance ->
                modelInstance?.let { instance ->
                    val newModelNode = ModelNode(
                        modelInstance = instance,
                        scaleToUnits = 1.0f,
                        centerOrigin = io.github.sceneview.math.Position(0f, 0f, 0f),
                    )

                    currentModelNode = newModelNode
                    currentZoomLevel = 1.0f // Reset zoom when car changes!
                    
                    // If we already have an anchor, update its child
                    currentAnchorNode?.let { anchorNode ->
                        anchorNode.childNodes.forEach { it.destroy() }
                        anchorNode.childNodes = setOf(newModelNode)
                    }
                }
            }
        }
    }

    // Handle Color Change for Paint and Rims
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
                            // "Old" implementation for 4.glb to overpower its baked-in red texture using emissive glow
                            material.setParameter("baseColorFactor", c.red, c.green, c.blue, c.alpha)
                            material.setParameter("emissiveFactor", c.red * 0.4f, c.green * 0.4f, c.blue * 0.4f)
                        } else {
                            // "New" mathematically correct implementation for properly textured cars
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
                        // Rims are generally metallic, convert to linear space
                        val r = Math.pow(c.red.toDouble(), 2.2).toFloat()
                        val g = Math.pow(c.green.toDouble(), 2.2).toFloat()
                        val b = Math.pow(c.blue.toDouble(), 2.2).toFloat()
                        material.setParameter("baseColorFactor", r, g, b, c.alpha)
                    }
                }
            }
        }
    }

    // Handle Interaction Modes
    LaunchedEffect(uiState.interactionMode, currentModelNode, currentAnchorNode) {
        val isTranslateMode = uiState.interactionMode == InteractionMode.TRANSLATE
        val isRotateMode = uiState.interactionMode == InteractionMode.ROTATE
        
        // 1. Lock/Unlock the AnchorNode from sliding across the floor
        currentAnchorNode?.isEditable = isTranslateMode
        currentAnchorNode?.isPositionEditable = isTranslateMode
        currentAnchorNode?.isRotationEditable = false
        currentAnchorNode?.isScaleEditable = false
        
        // 2. Configure the ModelNode (the car itself)
        currentModelNode?.let { node ->
            node.isEditable = isRotateMode || isTranslateMode
            node.isPositionEditable = false // The anchor handles position natively
            node.isRotationEditable = isRotateMode // Native two-finger twist
            node.isScaleEditable = false // We handle scaling manually for sensitivity and bounds
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            environment = environment,
            childNodes = childNodes,
            planeRenderer = !isCapturing,
            sessionConfiguration = { session, config ->
                config.lightEstimationMode = com.google.ar.core.Config.LightEstimationMode.AMBIENT_INTENSITY
            },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { e, node ->
                    // Only allow placing/teleporting the car if we are in Translate mode!
                    // If node == null, they tapped the floor.
                    if (node == null && uiState.interactionMode == InteractionMode.TRANSLATE) {
                        lastTapEvent = e
                    }
                },
                onScale = { detector, e, node ->
                    if (uiState.interactionMode == InteractionMode.ZOOM) {
                        currentModelNode?.let { modelNode ->
                            // Dampen sensitivity: native pinch is very fast. 
                            // Reduce the distance from 1.0f by multiplying by 0.3f
                            val scaleDelta = detector.scaleFactor - 1f
                            val adjustedScaleFactor = 1f + (scaleDelta * 0.3f)
                            
                            // Calculate new relative zoom level clamped between 0.5x and 5.0x of the DEFAULT size
                            val newZoomLevel = (currentZoomLevel * adjustedScaleFactor).coerceIn(0.5f, 5.0f)
                            val effectiveMultiplier = newZoomLevel / currentZoomLevel
                            currentZoomLevel = newZoomLevel
                            
                            val currentScale = modelNode.scale
                            modelNode.scale = io.github.sceneview.math.Scale(
                                currentScale.x * effectiveMultiplier,
                                currentScale.y * effectiveMultiplier,
                                currentScale.z * effectiveMultiplier
                            )
                        }
                    }
                }
            ),
            onSessionUpdated = { _, frame ->
                if (frame.camera.trackingState == TrackingState.TRACKING) {
                    lastTapEvent?.let { event ->
                        val hitResults = frame.hitTest(event)
                        val hitResult = hitResults.firstOrNull { hit ->
                            val trackable = hit.trackable
                            (trackable is Plane) && 
                            trackable.isPoseInPolygon(hit.hitPose) && 
                            (trackable.type == Plane.Type.HORIZONTAL_UPWARD_FACING)
                        }
                        
                        hitResult?.let { hit ->
                            val anchor = hit.createAnchor()
                            val anchorNode = AnchorNode(engine, anchor)
                            
                            currentModelNode?.let { modelNode ->
                                anchorNode.addChildNode(modelNode)
                            }
                            
                            childNodes.forEach { it.destroy() }
                            childNodes.clear()
                            childNodes.add(anchorNode)
                            currentAnchorNode = anchorNode
                        }
                        lastTapEvent = null
                    }
                }
            }
        )

        if (!isCapturing) {
            // Top Bar
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .oneUiEffect(shape = androidx.compose.foundation.shape.CircleShape)
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "AR Showroom", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text(text = "Tap floor to place car", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                }
            }

            // Back Button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 48.dp, start = 16.dp)
                    .oneUiEffect(shape = androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to Showroom",
                    tint = Color.White
                )
            }

            // Mute Button
            IconButton(
                onClick = { viewModel.handleIntent(CarShowroomIntent.ToggleMute) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 16.dp)
                    .oneUiEffect(shape = androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    imageVector = if (uiState.isMuted) androidx.compose.material.icons.Icons.Default.VolumeOff else androidx.compose.material.icons.Icons.Default.VolumeUp,
                    contentDescription = "Toggle Mute",
                    tint = Color.White
                )
            }

            // Interaction Mode Controls (Left Toolbar)
            val isPlaced = currentAnchorNode != null
            val accentColor = Color(0xFFFF3B30) // Bright Red
            
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lock / Translate Button
                IconButton(
                    onClick = { 
                        if (isPlaced) {
                            val newMode = if (uiState.interactionMode == InteractionMode.TRANSLATE) InteractionMode.LOCKED else InteractionMode.TRANSLATE
                            viewModel.handleIntent(CarShowroomIntent.SetInteractionMode(newMode))
                        }
                    },
                    modifier = Modifier.oneUiEffect(shape = androidx.compose.foundation.shape.CircleShape),
                    enabled = isPlaced
                ) {
                    Icon(
                        imageVector = if (uiState.interactionMode == InteractionMode.TRANSLATE) androidx.compose.material.icons.Icons.Default.LockOpen else androidx.compose.material.icons.Icons.Default.Lock,
                        contentDescription = "Toggle Translation",
                        tint = if (!isPlaced) Color.White.copy(alpha = 0.3f) 
                            else if (uiState.interactionMode == InteractionMode.TRANSLATE) accentColor 
                            else Color.White
                    )
                }

                // Zoom Button
                IconButton(
                    onClick = { 
                        if (isPlaced) {
                            val newMode = if (uiState.interactionMode == InteractionMode.ZOOM) InteractionMode.LOCKED else InteractionMode.ZOOM
                            viewModel.handleIntent(CarShowroomIntent.SetInteractionMode(newMode))
                        }
                    },
                    modifier = Modifier.oneUiEffect(shape = androidx.compose.foundation.shape.CircleShape),
                    enabled = isPlaced
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.ZoomIn,
                        contentDescription = "Toggle Zoom",
                        tint = if (!isPlaced) Color.White.copy(alpha = 0.3f) 
                            else if (uiState.interactionMode == InteractionMode.ZOOM) accentColor 
                            else Color.White
                    )
                }

                // Rotate Button
                IconButton(
                    onClick = { 
                        if (isPlaced) {
                            val newMode = if (uiState.interactionMode == InteractionMode.ROTATE) InteractionMode.LOCKED else InteractionMode.ROTATE
                            viewModel.handleIntent(CarShowroomIntent.SetInteractionMode(newMode))
                        }
                    },
                    modifier = Modifier.oneUiEffect(shape = androidx.compose.foundation.shape.CircleShape),
                    enabled = isPlaced
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                        contentDescription = "Toggle Rotate",
                        tint = if (!isPlaced) Color.White.copy(alpha = 0.3f) 
                            else if (uiState.interactionMode == InteractionMode.ROTATE) accentColor 
                            else Color.White
                    )
                }
            }
                
            // Camera (Snapshot) Button
            IconButton(
                onClick = { isCapturing = true },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(56.dp)
                    .oneUiEffect(shape = androidx.compose.foundation.shape.CircleShape),
                enabled = isPlaced
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Take Snapshot",
                    tint = if (!isPlaced) Color.White.copy(alpha = 0.3f) else Color.White
                )
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

            // Sheets
            androidx.compose.animation.AnimatedVisibility(
                visible = showCarSheet,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut()
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

            androidx.compose.animation.AnimatedVisibility(
                visible = showColorSheet,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut()
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

            androidx.compose.animation.AnimatedVisibility(
                visible = showWheelSheet,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp),
                enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + androidx.compose.animation.fadeOut()
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
}
